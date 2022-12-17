/*
 * Copyright (c) 2022. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driver.statelog

import androidx.room.Entity
import com.ingonoka.cba9driver.data.Denomination
import com.ingonoka.cba9driver.response.GetCountersResponseData
import com.ingonoka.cba9driver.util.Stringifiable
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Enum that identifies the type of log entry in the [Cba9StateLog]
 */
enum class Cba9StateLogEvent {
    /**
     * Only used to mark an event that has not yet been defined
     */
    UNDEFINED,

    /**
     * One and only one of the banknote counters must go up by exactly 1.
     */
    UPDATE_COUNTERS,

    /**
     * The cashbox is emptied. The banknote counters in this log entry are all zero.
     */
    EMPTY_CASHBOX,

    /**
     * The stacked and rejected properties are updated from the counters retrieved from the validator.
     * All banknote counters must be zero.
     */
    DEVICE_COUNTERS
}

/**
 * Database log entry in the [Cba9StateLog]
 */
@Entity(primaryKeys = ["event", "serialNumber", "time"])
class StateLogEntry(
    /**
     * Type of log entry (see [Cba9StateLogEvent]
     */
    val event: String,
    /**
     * Serial number of the BNA.  The log can hold entries for different BNAs, which may be necessary when the
     * BNA device is switched in a kiosk.
     */
    val serialNumber: Long,
    /**
     * The time of creating the entry.
     */
    val time: Instant,
    /**
     * Number of stacked banknotes since last reset of device counters. Only valid if the log entry is of
     * type [Cba9StateLogEvent.DEVICE_COUNTERS]
     */
    val stacked: Long,
    /**
     * Number of rejected banknotes since last reset of device counters. Only valid if the log entry is of
     * type [Cba9StateLogEvent.DEVICE_COUNTERS]
     */
    val rejected: Long,
    /**
     * Number of PHP20 banknotes. Only valid if the log entry is of type [Cba9StateLogEvent.UPDATE_COUNTERS]
     */
    val banknote20: Int,
    /**
     * Number of PHP50 banknotes. Only valid if the log entry is of type [Cba9StateLogEvent.UPDATE_COUNTERS]
     */
    val banknote50: Int,
    /**
     * Number of PHP100 banknotes. Only valid if the log entry is of type [Cba9StateLogEvent.UPDATE_COUNTERS]
     */
    val banknote100: Int,
    /**
     * Number of PHP200 banknotes. Only valid if the log entry is of type [Cba9StateLogEvent.UPDATE_COUNTERS]
     */
    val banknote200: Int,
    /**
     * Number of PHP500 banknotes. Only valid if the log entry is of type [Cba9StateLogEvent.UPDATE_COUNTERS]
     */
    val banknote500: Int,
    /**
     * Number of PHP1000 banknotes. Only valid if the log entry is of type [Cba9StateLogEvent.UPDATE_COUNTERS]
     */
    val banknote1000: Int
) : Stringifiable {

    /**
     * Total number of banknotes for this log entry.
     */
    val sumBanknotes: Int
        get() = banknote20 + banknote50 + banknote100 + banknote200 + banknote500 + banknote1000

    /**
     * Don't use directly.  Use [stateLogEntry] to create a state log entry.
     */
    class Dsl {

        var event: Cba9StateLogEvent = Cba9StateLogEvent.UNDEFINED
        var serialNumber: Long = 0
        var banknote20: Int = 0
        var banknote50: Int = 0
        var banknote100: Int = 0
        var banknote200: Int = 0
        var banknote500: Int = 0
        var banknote1000: Int = 0
        var stacked: Long = -1
        var rejected: Long = -1
        var time: Instant = Clock.System.now()

        fun banknoteCounters(counters: Map<Denomination, Int>) {
            counters.forEach { (denomination, count) ->
                when (denomination.denomination) {
                    20 -> banknote20 = count
                    50 -> banknote50 = count
                    100 -> banknote100 = count
                    200 -> banknote200 = count
                    500 -> banknote500 = count
                    1000 -> banknote1000 = count
                }
            }
        }

        fun countersResponseData(counters: GetCountersResponseData) {
            stacked = counters.stacked
            rejected = counters.rejected
        }

        fun build(): StateLogEntry {
            require(event != Cba9StateLogEvent.UNDEFINED)
            require(serialNumber != 0L)


            return StateLogEntry(
                event.name,
                serialNumber, time,
                stacked, rejected,
                banknote20, banknote50, banknote100, banknote200, banknote500, banknote1000
            )
        }
    }

    override fun stringify(short: Boolean, indent: String): String = if (short) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssx")
        val timeStr = time
            .toJavaInstant()
            .atOffset(ZoneOffset.UTC)
            .format(formatter)
        indent + "${Cba9StateLogEvent.valueOf(event)}, " +
                "sn:${serialNumber.toString(10)}, " +
                "time:${timeStr}, " +
                if (stacked != -1L) {
                    "stacked:${stacked.toString(10)}, rejected:${rejected.toString(10)}"
                } else {
                    "counter:[20=$banknote20,50=$banknote50,100=$banknote100,200=$banknote200,500=$banknote500,1000=$banknote1000]"
                }
    } else {
        ""
    }
}

inline fun stateLogEntry(block: StateLogEntry.Dsl.() -> Unit): StateLogEntry =
    StateLogEntry.Dsl().apply { block() }.build()
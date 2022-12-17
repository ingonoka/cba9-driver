/*
 * Copyright (c) 2022. Ingo Noka
 * This file belongs to project load-kiosk.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driver

import com.ingonoka.cba9driver.Cba9CashboxState.*
import com.ingonoka.cba9driver.data.Denomination
import com.ingonoka.cba9driver.data.SerialNumber
import com.ingonoka.cba9driver.statelog.ICba9StateLog
import com.ingonoka.cba9driver.statelog.StateLogEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Instant
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.math.roundToInt

/**
 * Fill level state of cashbox
 */
enum class Cba9CashboxState {
    UNKNOWN,

    /**
     * The cashbox is empty
     */
    EMPTY,

    /**
     * The cashbox is being filled.
     */
    FILLING,

    /**
     * The cashbox is full.
     */
    FULL
}

/**
 * Represents a cashbox in a banknote acceptor.  Keeps track of fill level, which is measured in relation to
 * the maximum number of banknotes that fit into the cash box ([capacity]).  The cashbox is linked to
 * a validator with serial number [serialNumber], which will be used to log fill levels specific to the validator.
 *
 * The cashbox status can be tracked by subscribing to [state].
 *
 * The fill level can be tracked by observing [levels]
 *
 */
class Cba9Cashbox(
    private val stateLog: ICba9StateLog,
    private val capacity: Int,
    private val maxLoadBeforeCollection: Int,
    val serialNumber: SerialNumber
) {

    val logger: Logger = LoggerFactory.getLogger(this::class.java.name)

    private val _levels = MutableStateFlow<CashboxLevelHolder>(CashboxLevelHolder.UNKNOWN)

    /**
     * Observe this state to react to change in fill level
     */
    val levels = _levels.asStateFlow()

    private val _state = MutableStateFlow(UNKNOWN)

    /**
     * Observe this state flow to react to change in the state of the cashbox
     */
    val state = _state as StateFlow<Cba9CashboxState>

    private val banknoteCounters = mutableMapOf<Denomination, Int>()

    init {
        if (serialNumber.value != 0L) {
            val countersFromDb = stateLog
                .getCurrentCounters(serialNumber.value)
                .onSuccess { logger.trace("Read counters from log: $this") }
                .getOrElse { throw Exception("Couldn't get saved counters from database", it) }

            setBanknoteCount(countersFromDb)
        }
    }

    /**
     * Set cashbox state to full
     */
    fun setFull() {
        if (_state.value != FULL) {
            _state.value = FULL
        } else {
            logger.warn("Setting state of cashbox to FULL even so state is already FULL")
        }
    }

    /**
     * Set cashbox state to empty.
     */
    fun setEmpty(time: Instant) {
        if (_state.value != EMPTY) {
            _state.value = EMPTY
            stateLog.emptyCashbox(time, serialNumber.value).getOrThrow()
            stateLog.updateCounters(time, serialNumber.value, mapOf()).getOrThrow()

        } else {
            logger.warn("Setting state of cashbox to EMPTY even so state is already EMPTY")
        }
        banknoteCounters.clear()
        _levels.update {
            CashboxLevelHolder.CashboxLevel(numberOfBanknotes(), valueOfBanknotes(), level())
        }
    }

    /**
     * The time of the most recent empty cashbox entry in the log
     *
     * Return [Instant.DISTANT_PAST] if no entry was found.
     */
    fun getTimeOfLastEmpty(): Result<Instant> = stateLog.getMostRecentEmptyTime(serialNumber.value)

    /**
     * The time of the most recent empty cashbox entry in the log
     *
     * Return [Instant.DISTANT_PAST] if no entry was found.
     */
    fun getMostRecentAudit(): Result<StateLogEntry> = stateLog.getMostRecentAudit(serialNumber.value)

    /**
     * Set cashbox state to filling (neither empty nor full)
     */
    private fun setFilling() {

        if (_state.value == FULL) {
            logger.warn("Rejected attempt to set state of cashbox to FILLING even so state is already FULL.")
        } else {
            _state.value = FILLING
        }
    }

    /**
     * Approx. fill level of cashbox in percent.
     */
    fun level(): Int = ((banknoteCounters.values.sum().toFloat() / capacity) * 100.0).roundToInt()

    /**
     * Approx. number of banknotes in the cashbox.
     */
    fun numberOfBanknotes(): Int = banknoteCounters.values.sum()

    /**
     * Approx. value of banknotes in cashbox (Pesos)
     */
    fun valueOfBanknotes(): Int = banknoteCounters.entries.sumOf {
        it.key.denomination * it.value
    }

    /**
     * Get number of banknotes by denomination currently in the cashbox.
     */
    fun getBanknoteCount(): Map<Denomination, Int> = banknoteCounters.toMap()

    /**
     * Get number of banknotes by denomination currently in the cashbox as String that can be saved to properties.
     *
     * The banknote counters are sorted by denomination value, i.e. PHP20:1,PHP50:3 etc.  Banknote values for which the counter
     * is zero are not included.
     */
    fun getBanknoteCountAsString(): String = banknoteCounters.entries.filter {
        it.value != 0
    }.sortedBy {
        it.key.denomination
    }.joinToString(",") {
        "%s%d:%d".format(it.key.countryCode, it.key.denomination, it.value)
    }

    /**
     * Set the filling state (count per banknote denomination).
     *
     * All counters will be cleared first.
     */
    fun setBanknoteCount(counters: Map<Denomination, Int>): Result<Unit> = try {

        val sumNewCounters = counters.values.sum()
        when {
            sumNewCounters > capacity -> throw Exception("Setting counters would exceed the capacity of the cashbox.")
            sumNewCounters == capacity -> setFull()
            sumNewCounters > 0 -> setFilling()
        }

        banknoteCounters.clear()
        banknoteCounters.putAll(counters)
        _levels.update {
            CashboxLevelHolder.CashboxLevel(numberOfBanknotes(), valueOfBanknotes(), level())
        }

        Result.success(Unit)

    } catch (e: Exception) {

        Result.failure(Exception("Failed to set banknote counters", e))
    }

    /**
     * Add a banknote to the cashbox (for tracking purposes)
     */
    fun addBanknote(time: Instant, denomination: Denomination) {
        banknoteCounters[denomination] = banknoteCounters[denomination]?.plus(1) ?: 1

        stateLog.updateCounters(time, serialNumber.value, banknoteCounters).getOrThrow()

        _levels.update {
            CashboxLevelHolder.CashboxLevel(numberOfBanknotes(), valueOfBanknotes(), level())
        }

        if (numberOfBanknotes() > capacity || valueOfBanknotes() > maxLoadBeforeCollection) {
            logger.error("Adding banknote to cashbox is already filled to capacity ")
            setFull()
        } else {
            setFilling()
        }
    }

    sealed class CashboxLevelHolder(
        val banknoteCount: Int,
        val banknoteValue: Int,
        val fillLevel: Int
    ) {
        object UNKNOWN : CashboxLevelHolder(-1, -1, -1)
        class CashboxLevel(banknoteCount: Int, banknoteValue: Int, fillLevel: Int) :
            CashboxLevelHolder(banknoteCount, banknoteValue, fillLevel)
    }
}
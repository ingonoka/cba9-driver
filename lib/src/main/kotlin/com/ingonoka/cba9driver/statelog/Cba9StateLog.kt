/*
 * Copyright (c) 2022. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driver.statelog

import com.ingonoka.cba9driver.data.Denomination
import com.ingonoka.cba9driver.response.GetCountersResponseData
import kotlinx.datetime.Instant

/**
 * Interface to a log of cashbox related events such as cash collection, reading of audit counters from
 * the CBA9, adding of banknotes etc.
 *
 * The log keeps records for multiple BNAs based on the serial number of the BNA.
 */
interface ICba9StateLog {

    /**
     * Remove all entries that are older than [time]
     */
    fun deleteOlderThan(time: Instant)

    /**
     * Get all [StateLogEntry]s for device with [serialNumber]
     */
    fun getAllRecords(serialNumber: Long): List<StateLogEntry>

    /**
     * Add a log entry with the current counters. Ideally only one counter should have changed compared to
     * the previous log entry.
     */
    fun updateCounters(time: Instant, serialNumber: Long, counters: Map<Denomination, Int>): Result<Unit>

    /**
     * Log the emptying of the cashbox.  The counters in this log entry will all be zero.
     */
    fun emptyCashbox(time: Instant, serialNumber: Long): Result<Unit>

    /**
     * Log the counters as read from the device with `getCounters`.  This would usually be a good idea
     * at startup and after the cashbox was emptied.  The audit entries can be used to check the
     * integrity of the banknote counters.
     */
    fun updateDeviceCounters(
        time: Instant,
        serialNumber: Long,
        getCountersResponseData: GetCountersResponseData
    ): Result<Unit>

    /**
     * Get the counters from the latest log entry.  This will be used to initialize the banknote counters of
     * the cashbox on startup.  NOTE: Don't confuse this with the stacked/reject counters that are retrieved from
     * the device itself.
     */
    fun getCurrentCounters(serialNumber: Long): Result<Map<Denomination, Int>>

    /**
     * Get most recent audit counters (the ones read from the device itself)
     */
    fun getMostRecentAudit(serialNumber: Long): Result<StateLogEntry>

    /**
     * Return [Instant] of most recent empty cashbox entry in log.
     *
     * Return [Instant.DISTANT_PAST] if no such entry exists.
     */
    fun getMostRecentEmptyTime(serialNumber: Long): Result<Instant>

    /**
     * Audit the state log.  Function verifies whether the banknote counters that the driver keeps track off
     * match the "stacked" counter that the device keep track off.  The total number of banknotes that
     * have been recorded in between two device counter log entries must match the difference between the
     * two device counter entries.  The device counters should not be reset during the lifetime of the device.
     */
    suspend fun audit(serialNumber: Long): Result<Boolean>

    /**
     * Perform any housekeeping before the [ICba9StateLog] is closed.  For example, the underlying
     * data store may need to be flushed and closed.
     */
    fun close()
}


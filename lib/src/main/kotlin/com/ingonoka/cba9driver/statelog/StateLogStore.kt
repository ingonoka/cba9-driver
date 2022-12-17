package com.ingonoka.cba9driver.statelog

import kotlinx.datetime.Instant

/**
 * Interface to a store of [StateLogEntry]s.
 *
 * @see InMemoryStateLogStore
 * @see RoomStateLogStore
 */
interface StateLogStore {
    /**
     * Insert all [logEntries] into the store.
     */
    fun insertAll(vararg logEntries: StateLogEntry)

    /**
     * Get all entries for BNA with [serialNumber]
     */
    fun getAll(serialNumber: Long): List<StateLogEntry>

    /**
     * Delete all entries that are older than [time]
     */
    fun deleteOlderThan(time: Instant)

    /**
     * Get the most recent counters entry from teh state log. Immediately after a cash collection, this function
     * should return 0 for all denominations, then after banknotes have been stacked, this function would
     * return the number of banknotes for the respective denomination of the banknote and so forth.
     */
    fun getMostRecentCounters(serialNumber: Long): Result<List<StateLogEntry>>

    /**
     * Get the log entry for the most recent counters as read from the CBA9
     */
    fun getMostRecentAudit(serialNumber: Long): StateLogEntry?

    /**
     * Get the log entry of the most recent cash collection
     */
    fun getMostRecentEmpty(serialNumber: Long): StateLogEntry?

    /**
     * Close the store. After this, accessing any of the functions would result in an error.
     */
    fun close()
}
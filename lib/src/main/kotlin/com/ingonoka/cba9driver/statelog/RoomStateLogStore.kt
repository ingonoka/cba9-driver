package com.ingonoka.cba9driver.statelog

import kotlinx.datetime.Instant

/**
 * A [StateLogStore] that stores [StateLogEntry]s in a Room database.
 */
class RoomStateLogStore(private val db: StateLogDb): StateLogStore {

    override fun insertAll(vararg logEntries: StateLogEntry) {
        db.stateLogDao().insertAll(*logEntries)
    }

    override fun getAll(serialNumber: Long): List<StateLogEntry> = db.stateLogDao().getAll(serialNumber)

    override fun deleteOlderThan(time: Instant) = db.stateLogDao().deleteOlderThan(time.toEpochMilliseconds())

    override fun getMostRecentCounters(serialNumber: Long): Result<List<StateLogEntry>> = try {

        val counters = db.stateLogDao().getMostRecentCounters(serialNumber)

        Result.success(counters)

    } catch(e: Exception) {

        Result.failure(e)
    }
    override fun getMostRecentAudit(serialNumber: Long): StateLogEntry? =
        db.stateLogDao().getMostRecentAudit(serialNumber)

    override fun getMostRecentEmpty(serialNumber: Long): StateLogEntry? =
        db.stateLogDao().getMostRecentEmpty(serialNumber)

    override fun close() = db.close()

}
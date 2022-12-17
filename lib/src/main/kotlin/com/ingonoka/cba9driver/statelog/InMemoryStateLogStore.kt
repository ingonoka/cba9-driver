package com.ingonoka.cba9driver.statelog

import kotlinx.datetime.Instant

/**
 * A [StateLogStore] that stores [StateLogEntry]s in memory only. This is for testing purposes!  Data will be lost
 * when application is restarted.
 */
class InMemoryStateLogStore() : StateLogStore {

    private val log = mutableListOf<StateLogEntry>()

    override fun insertAll(vararg logEntries: StateLogEntry) {
        log.addAll(logEntries)
    }

    override fun getAll(serialNumber: Long): List<StateLogEntry> = log.toList().filter { it.serialNumber == serialNumber }

    override fun deleteOlderThan(time: Instant) {
        log.removeIf { it.time < time }
    }

    override fun getMostRecentCounters(serialNumber: Long): Result<List<StateLogEntry>> =
        Result.success(log.filter {
            it.event == Cba9StateLogEvent.UPDATE_COUNTERS.name
        }.maxByOrNull {
            it.time
        }?.let { listOf(it) } ?: listOf())

    override fun getMostRecentAudit(serialNumber: Long): StateLogEntry? =
        log.filter {
            it.event == Cba9StateLogEvent.DEVICE_COUNTERS.name
        }.maxByOrNull {
            it.time
        }

    override fun getMostRecentEmpty(serialNumber: Long): StateLogEntry? =
        log.filter {
            it.event == Cba9StateLogEvent.EMPTY_CASHBOX.name
        }.maxByOrNull {
            it.time
        }

    override fun close() = Unit
}
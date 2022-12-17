/*
 * Copyright (c) 2022. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

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
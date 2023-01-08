/*
 * Copyright (c) 2023. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driver.statelog

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface StateLogDao {

    @Insert
    fun insertAll(vararg logEntries: StateLogEntry)

    @Query("SELECT * FROM StateLogEntry WHERE serialNumber = :serialNumber ORDER BY time")
    fun getAll(serialNumber: Long): List<StateLogEntry>

    @Query("DELETE FROM StateLogEntry WHERE time < ( :time )")
    fun deleteOlderThan(time: Long)

    @Query("SELECT * FROM StateLogEntry WHERE time = (SELECT MAX(time) FROM StateLogEntry WHERE serialNumber = :serialNumber AND event <> 'DEVICE_COUNTERS')")
    fun getMostRecentCounters(serialNumber: Long): List<StateLogEntry>

    @Query("SELECT * FROM StateLogEntry WHERE time = (SELECT MAX(time) FROM StateLogEntry WHERE serialNumber = :serialNumber AND event = 'DEVICE_COUNTERS')")
    fun getMostRecentAudit(serialNumber: Long): StateLogEntry?

    @Query("SELECT * FROM StateLogEntry WHERE time = (SELECT MAX(time) FROM StateLogEntry WHERE serialNumber = :serialNumber AND event = 'EMPTY_CASHBOX')")
    fun getMostRecentEmpty(serialNumber: Long): StateLogEntry?

}
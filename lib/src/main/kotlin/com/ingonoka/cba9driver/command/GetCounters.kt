/*
 * Copyright (c) 2022. Ingo Noka
 * This file belongs to project load-kiosk.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driver.command

import com.ingonoka.cba9driver.response.GenericResponseCode
import com.ingonoka.cba9driver.response.GetCountersResponseData
import com.ingonoka.cba9driver.util.IntBuffer
import com.ingonoka.usbmanager.IUsbTransceiver

/**
 * The command to return a global note activity counter set for the slave device. The response is formatted as in the
 * table below and the counter values are persistent in memory after a power-down/power-up cycle.
 * These counters are not set independent and will wrap to zero and begin again if their maximum value is reached.
 * Each counter is made up of 4 bytes of data giving a max value of 4294967295.
 * ```
 * byte offset      |  function
 * -------------------------------------------------------------
 * 0                | Generic OK
 * 1                | Number of counters in set
 * 2                | Stacked
 * 6                | Stored
 * 10               | Dispensed
 * 14               | Transferred to stack
 * 18               | Rejected
 * -------------------------------------------------------------
 *
 * ```
 */
class GetCounters : SspCommand(SspCommandCode.GetCounters)

suspend fun IUsbTransceiver.getCounters(): Result<GetCountersResponseData> = try {

    GetCounters().run(this).onSuccess {
        if (it.genericResponseCode != GenericResponseCode.OK)
            throw Exception("Failure response: ${it.genericResponseCode.convertToMetaCode(SspCommandCode.Hold)}")

    }.map {
        val bytes = it.data

        if (bytes.isEmpty())
            throw Exception("Command to get counters returned no data")

        GetCountersResponseData.decode(IntBuffer.wrap(bytes)).getOrThrow()

    }

} catch (e: Exception) {

    Result.failure(Exception("Failed execution of get counters.", e))
}
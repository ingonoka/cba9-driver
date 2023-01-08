/*
 * Copyright (c) 2023. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driver.command

import com.ingonoka.cba9driver.response.GenericResponseCode
import com.ingonoka.cba9driver.response.UnitDataResponseData
import com.ingonoka.cba9driver.util.IntBuffer
import com.ingonoka.usbmanager.IUsbTransceiver

/**
 * A command to return version information about the connected device to the format described in the table below:
 *
 * ```
 *
 * byte offset      |  function
 * -------------------------------------------------------------
 *
 * 0                | Generic OK Response (OxF0) 1
 * 1                | Unit type
 * 2                | Firmware version (4 byte ASCII)
 * 6                | Dataset country (3 byte ASCII)
 * 9                | Value multiplier
 * 12               | Protocol version
 *  -------------------------------------------------------------
 *
 *```
 */
class UnitData : SspCommand(SspCommandCode.UnitData)

suspend fun getUnitData(transceiver: IUsbTransceiver): Result<UnitDataResponseData> = try {

    UnitData().run(transceiver).onSuccess {
        if (it.genericResponseCode != GenericResponseCode.OK)
            throw Exception("Command to get unit data failed: ${it.genericResponseCode}")
    }.map { response ->
        val bytes = IntBuffer.wrap(response.data)

        if (!bytes.hasBytesLeftToRead())
            throw Exception("Command to get unit data returned no data")

        UnitDataResponseData.decode(bytes).getOrThrow()

    }

} catch (e: Exception) {

    Result.failure(Exception("Failed execution of get unit data.", e))
}

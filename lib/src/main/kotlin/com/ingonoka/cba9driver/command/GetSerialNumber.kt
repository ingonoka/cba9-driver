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
import com.ingonoka.cba9driver.response.GetSerialNumberResponseData
import com.ingonoka.cba9driver.util.listOfIntBuffer
import com.ingonoka.usbmanager.IUsbTransceiver

/**
 * This command returns a 4-byte big endian array representing the unique factory programmed serial number of the device.
 * Returning `0x1c962c` is equal to serial number `01873452`
 */
class GetSerialNumber : SspCommand(SspCommandCode.GetSerialNumber)

suspend fun IUsbTransceiver.getSerialNumber(): Result<GetSerialNumberResponseData> = try {

    GetSerialNumber().run(this).onSuccess {
        if (it.genericResponseCode != GenericResponseCode.OK)
            throw Exception("Failure response: ${it.genericResponseCode.convertToMetaCode(SspCommandCode.Hold)}")
    }.map {
        val bytes = it.data

        if (bytes.isEmpty())
            throw Exception("Command to get serial number returned no data")

        GetSerialNumberResponseData.decode(bytes.listOfIntBuffer()).getOrThrow()
    }

} catch (e: Exception) {

    Result.failure(Exception("Failed execution of get serial number.", e))
}

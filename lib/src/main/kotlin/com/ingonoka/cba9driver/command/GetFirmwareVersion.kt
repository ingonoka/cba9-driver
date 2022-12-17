/*
 * Copyright (c) 2022. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driver.command

import com.ingonoka.cba9driver.response.GenericResponseCode
import com.ingonoka.cba9driver.response.GetFirmwareVersionResponseData
import com.ingonoka.cba9driver.util.IntBuffer
import com.ingonoka.usbmanager.IUsbTransceiver

/**
 * Returns a variable length ASCII array containing the full firmware version of the attached device.
 */
class GetFirmwareVersion : SspCommand(SspCommandCode.GetFirmwareVersion)

suspend fun IUsbTransceiver.getFirmwareVersion(): Result<GetFirmwareVersionResponseData> = try {

    GetFirmwareVersion().run(this).onSuccess {
        if (it.genericResponseCode != GenericResponseCode.OK)
            throw Exception("Failure response: ${it.genericResponseCode.convertToMetaCode(SspCommandCode.Hold)}")
    }.map {
        val bytes = it.data

        if (bytes.isEmpty())
            throw Exception("Command to get dataset version returned no data")

        GetFirmwareVersionResponseData.decode(IntBuffer.wrap(bytes)).getOrThrow()

    }
} catch (e: Exception) {

    Result.failure(Exception("Failed execution of get dataset version.", e))
}
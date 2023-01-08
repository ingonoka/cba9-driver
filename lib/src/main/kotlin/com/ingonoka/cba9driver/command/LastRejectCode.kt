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
import com.ingonoka.cba9driver.response.LastRejectCodeResponseData
import com.ingonoka.cba9driver.util.IntBuffer
import com.ingonoka.usbmanager.IUsbTransceiver

/**
 * Returns a one byte code representing the reason the BNV rejected the last note. See Reject Code Table at the
 * start of the manual for more information.
 */
class LastRejectCode : SspCommand(SspCommandCode.LastRejectCode)

suspend fun getLastRejectCode(transceiver: IUsbTransceiver): Result<LastRejectCodeResponseData> = try {

    LastRejectCode().run(transceiver).onSuccess {
        if (it.genericResponseCode != GenericResponseCode.OK)
            throw Exception("Failure response: ${it.genericResponseCode.convertToMetaCode(SspCommandCode.Hold)}")
    }.map {
        val bytes = IntBuffer.wrap(it.data)

        if (bytes.hasBytesLeftToRead())
            throw Exception("Command to get last reject code returned no data")

        LastRejectCodeResponseData.decode(bytes).getOrThrow()
    }
} catch (e: Exception) {

    Result.failure(Exception("Failed execution of get last reject code.", e))
}
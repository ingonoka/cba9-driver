/*
 * Copyright (c) 2022. Ingo Noka
 * This file belongs to project load-kiosk.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driver.command

import com.ingonoka.cba9driver.Cba9Validator
import com.ingonoka.cba9driver.response.GenericResponseCode
import com.ingonoka.cba9driver.response.PollResponseData
import com.ingonoka.cba9driver.util.IntBuffer
import com.ingonoka.usbmanager.IUsbTransceiver

/**
 * This command returns a list of events occurred in the device since the last poll was sent.
 *
 * A single response can contain multiple events. The first event to have occurred will be at the
 * start of the packet.
 */
class Poll : SspCommand(SspCommandCode.Poll)

suspend fun IUsbTransceiver.poll(validator: Cba9Validator): Result<PollResponseData> = try {

    Poll().run(this).onSuccess {
        if (it.genericResponseCode != GenericResponseCode.OK)
            throw Exception("Failure response: ${it.genericResponseCode.convertToMetaCode(SspCommandCode.Hold)}")
    }.map {
        val bytes = it.data

        PollResponseData.decode(IntBuffer.wrap(bytes), validator.configData.bankNoteDenominations).getOrThrow()
    }

} catch (e: Exception) {

    Result.failure(Exception("Failed execution of poll.", e))
}
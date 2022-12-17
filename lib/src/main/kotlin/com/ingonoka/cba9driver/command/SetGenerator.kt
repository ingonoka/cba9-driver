/*
 * Copyright (c) 2022. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driver.command

import com.ingonoka.cba9driver.command.SspCommandCode.SetGenerator
import com.ingonoka.cba9driver.response.GenericResponseCode
import com.ingonoka.cba9driver.response.SspResponse
import com.ingonoka.usbmanager.IUsbTransceiver

/**
 * Part of the eSSP encryption negotiation sequence. Eight data bytes are sent. This is a 64-bit number representing
 * the Generator and must be a prime number. The slave will reply with OK or PARAMETER_OUT_OF_RANGE if the number is
 * not prime.
 */
class SetGenerator(generator: List<Int>) : SspCommand(SetGenerator, generator)


suspend fun IUsbTransceiver.setGenerator(generator: List<Int>): Result<SspResponse> = try {

    SetGenerator(generator)
        .run(this)
        .onSuccess {
            if (it.genericResponseCode != GenericResponseCode.OK)
                throw Exception("Command to set generator failed: ${it.genericResponseCode.convertToMetaCode(SetGenerator)}")
        }

} catch (e: Exception) {

    Result.failure(Exception("Failed execution of set generator.", e))
}
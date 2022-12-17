/*
 * Copyright (c) 2022. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driver.command

import com.ingonoka.cba9driver.command.SspCommandCode.Hold
import com.ingonoka.cba9driver.response.GenericResponseCode
import com.ingonoka.cba9driver.response.SspResponse
import com.ingonoka.usbmanager.IUsbTransceiver

/**
 * SSP banknote validators include a poll timeout of 10 seconds. If a new poll is not received within this time, then
 * a note held in escrow will be rejected. The host may require that the note is continued to be held, but a new poll
 * would accept the note. Sending this command (or any other command except poll) will reset the timeout and continue
 * to hold the note in escrow until such time as either a reject or poll command is sent.
 *
 * If there is no note in escrow then a COMMAND_CANNOT_BE_PROCESSED error will be sent.
 */
class Hold : SspCommand(Hold)

suspend fun IUsbTransceiver.holdBanknote(): Result<SspResponse> = try {

    Hold()
        .run(this)
        .onSuccess {
            if (it.genericResponseCode != GenericResponseCode.OK)
                throw Exception("Command to hold banknote failed: ${it.genericResponseCode.convertToMetaCode(Hold)}")
        }

} catch (e: Exception) {

    Result.failure(Exception("Failed execution of Hold.", e))
}
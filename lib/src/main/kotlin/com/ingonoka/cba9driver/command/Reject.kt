/*
 * Copyright (c) 2022. Ingo Noka
 * This file belongs to project load-kiosk.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driver.command

import com.ingonoka.cba9driver.command.SspCommandCode.Reject
import com.ingonoka.cba9driver.response.GenericResponseCode
import com.ingonoka.cba9driver.response.SspResponse
import com.ingonoka.usbmanager.IUsbTransceiver

/**
 * After a banknote validator device reports a valid note is held in escrow, this command may be sent to cause the
 * banknote to be rejected back to the user.
 *
 * Returns COMMAND_CANNOT_BE_PROCESSED if no note is in escrow.
 */
class Reject : SspCommand(Reject)

suspend fun IUsbTransceiver.ejectBanknote(): Result<SspResponse> = try {

    Reject().run(this).onSuccess {
        if (it.genericResponseCode != GenericResponseCode.OK) {
            throw Exception("Failure response: ${it.genericResponseCode.convertToMetaCode(SspCommandCode.Hold)}")
        }
    }

} catch (e: Exception) {

    Result.failure(Exception("Failed execution of reject banknote.", e))
}
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
import com.ingonoka.cba9driver.response.SspResponse
import com.ingonoka.usbmanager.IUsbTransceiver

/**
 * Allows the host to control the illumination of the bezel. Send this command to show bezel illumination when the
 * device is enabled for banknote validation. (This is the default condition at reset). Note that the validator will s
 * till override the illumination of the bezel, i.e. the bezel will not be illuminated if the device is not enabled
 * even if this command is sent.
 */
class DisplayOn : SspCommand(SspCommandCode.DisplayOn)

suspend fun IUsbTransceiver.displayOn(): Result<SspResponse> = try {

    DisplayOn().run(this)
        .onSuccess {
            if (it.genericResponseCode != GenericResponseCode.OK)
                throw Exception("Failure response: ${it.genericResponseCode.convertToMetaCode(SspCommandCode.Hold)}")
        }

} catch (e: Exception) {

    Result.failure(Exception("Failed execution DisplayOn.", e))
}
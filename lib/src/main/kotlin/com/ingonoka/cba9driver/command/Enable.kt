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
import com.ingonoka.cba9driver.response.SspResponse
import com.ingonoka.usbmanager.IUsbTransceiver

/**
 * This command will enable the SSP device for normal operation. The banknote validator will commence
 * validating banknotes entered into its bezel.
 *
 * This command may not create an event (as returned by the POLL command).  A meta event ENABLED should be
 * generated and send to the Cba9Validator object to update the tracked state.
 */
class Enable: SspCommand(SspCommandCode.Enable)

suspend fun IUsbTransceiver.enable(): Result<SspResponse> = try {

    Enable().run(this)
        .onSuccess {
            if (it.genericResponseCode != GenericResponseCode.OK)
                throw Exception("Failure response: ${it.genericResponseCode.convertToMetaCode(SspCommandCode.Hold)}")
        }

} catch (e: Exception) {

    Result.failure(Exception("Failed execution of Enable.", e))
}
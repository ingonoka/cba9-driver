/*
 * Copyright (c) 2023. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driver.command

import com.ingonoka.cba9driver.command.SspCommandCode.HostProtocolVersion
import com.ingonoka.cba9driver.response.GenericResponseCode
import com.ingonoka.cba9driver.response.SspResponse
import com.ingonoka.usbmanager.IUsbTransceiver

/**
 * ITL SSP devices use a system of protocol levels to control the event responses to polls to ensure that changes
 * would not affect systems with finite state machines unable to test for new events with non-defined data lengths.
 *
 * Use this command to allow the host to set which protocol version to operate the slave device.
 *
 * If the device supports the requested protocol OK (0xF0) will be returned. If not then FAIL (0xF8) will be returned
 */
class HostProtocolVersion(version: Int) : SspCommand(HostProtocolVersion, listOf(version and 0xFF))

suspend fun IUsbTransceiver.setHostProtocolVersion(version: Int): Result<SspResponse> = try {

    HostProtocolVersion(version)
        .run(this)
        .onSuccess {
            if (it.genericResponseCode != GenericResponseCode.OK)
                throw Exception("Failure response: ${it.genericResponseCode.convertToMetaCode(SspCommandCode.Hold)}")
        }

} catch (e: Exception) {

    Result.failure(Exception("Failed execution of setting protocol version.", e))
}
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
 * SSP uses a system of sequence bits to ensure that packets have been received by the slave and the reply received
 * by the host. If the slave receives the same sequence bit as the previous command packet then this is signal to
 * re-transmit the last reply. A mechanism is required to initially set the host and slave to the same sequence bits
 * and this is done by the use of the SYNC command. A Sync command resets the seq bit of the packet so that the slave
 * device expects the next seq bit to be 0. The host then sets its next seq bit to 0 and the seq sequence is synchronised.
 *
 * The SYNC command should be the first command sent to the slave during a session.
 */
class Sync : SspCommand(SspCommandCode.Sync)

suspend fun IUsbTransceiver.sync(): Result<SspResponse> = try {

    Sync().run(this)
        .onSuccess {
            if (it.genericResponseCode != GenericResponseCode.OK)
                throw Exception("Failure response: ${it.genericResponseCode.convertToMetaCode(SspCommandCode.Hold)}")
        }

} catch (e: Exception) {

    Result.failure(Exception("Failed execution of Sync.", e))
}
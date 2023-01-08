/*
 * Copyright (c) 2023. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driver.command

import com.ingonoka.cba9driver.data.Denomination
import com.ingonoka.cba9driver.data.Denominations
import com.ingonoka.cba9driver.response.SspResponse
import com.ingonoka.usbmanager.IUsbTransceiver

/**
 * Sets the channel inhibit level for the device. Each byte sent represents 8 bits (channels of inhibit). The first
 * byte is channels 1-8, second byte is 9-16 etc. The other BNV devices have the option of sending 1 or 2 bytes for
 * 8 or 16 channel operation. Any channels not included in the request will be inhibited (e.g. sending 1 byte
 * inhibits channels 9+). Set the bit low to inhibit all note acceptance on that channel, high to allow note acceptance.
 */
class SetInhibits(inhibits: List<Int>) : SspCommand(SspCommandCode.SetInhibits, inhibits)

suspend fun IUsbTransceiver.setInhibits(inhibits: List<Int>) = SetInhibits(inhibits).run(this)

/**
 * Set inhibits for [denominationsToBeAccepted] based on [denominationsSupported].
 *
 * Use [denominationsSupported] to find the channel number for the denominations to be inhibited.
 */
suspend fun IUsbTransceiver.setInhibits(
    denominationsSupported: Denominations,
    denominationsToBeAccepted: List<Denomination>
): Result<SspResponse> = setInhibits(denominationsSupported.getInhibits(denominationsToBeAccepted))
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
import com.ingonoka.cba9driver.response.SetupResponseData
import com.ingonoka.cba9driver.util.IntBuffer
import com.ingonoka.usbmanager.IUsbTransceiver

/**
 * Request the setup configuration of the device. Gives detail about versions, channel assignments, country codes
 * and values. Each device type has a different return data format. Please refer to the device information table at
 * the beginning of the manual for individual device data formats.
 *
 * ```
 *
 * byte offset      |  function                     | Note
 * --------------------------------------------------------------------------------------------------------------------
 * 0                | Unit type                     | Banknote validator
 * 1                | Firmware version              | ASCII data of device firmware version (e.g. '0110' = 1.10)
 * 5                | Country code 5                | ASCII code of the device dataset (e.g. 'EUR')
 * 8                | Value Multiplier 8            | The value to multiply the individual channels by to get the full
 *                  |                               | value. If this value is 0 then it indicates that this is a protocol
 *                  |                               | version 6 or greater compatible dataset where the values are given
 *                  |                               | in the expanded segment of the return data.
 * 11               | Number of channels (n)        | he highest channel used in this device dataset
 * 12               | Channel Values                | A variable size array of byes, 1 for each channel with a value
 *                  |                               | from 1 to 255 which when multiplied by the value multiplier gives
 *                  |                               | the full value of the note. If the value multiplier is zero then
 *                  |                               | these values are zero.
 * 12 + n           | Channel Security              | An obsolete value showing security level. This is set to 2 if the
 *                  |                               | value multiplier is > 0 otherwise 0.
 * 12 + (n*2)       | Real value Multiplier         | The value by which the channel values can be multiplied to show
 *                  |                               | their full value e.g. 5.00 EUR = 500 EUR cents
 * 15 + (n*2)       | Protocol version              | The current protocol version set for this device
 * 16 + (n*2)       | Expanded channel country code | Three byte ascii code for each channel. This allows multi currency
 *                  |                               | datasets to be used on SSP devices. These bytes are given only on
 *                  |                               | protocol versions >= 6.
 * 16 + (n*2)       | Expanded channel value        | bytes for each channel value. These bytes are given only on protocol
 *                  |                               |versions >= 6.
 * --------------------------------------------------------------------------------------------------------------------
 *
 * ```
 */
class SetupRequest : SspCommand(SspCommandCode.SetupRequest)

suspend fun IUsbTransceiver.getConfig(): Result<SetupResponseData> = try {

    SetupRequest().run(this).onSuccess {
        if (it.genericResponseCode != GenericResponseCode.OK)
            throw Exception("Command to get setup data failed: ${it.genericResponseCode}")
    }.map {
        val buf = IntBuffer.wrap(it.data)

        if (!buf.hasBytesLeftToRead())
            throw Exception("Command to get setup data returned no data")

        SetupResponseData.decode(buf).getOrThrow()
    }
} catch (e: Exception) {

    Result.failure(Exception("Failed execution of get setup data.", e))
}

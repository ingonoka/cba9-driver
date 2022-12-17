/*
 * Copyright (c) 2022. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driver.command

import com.ingonoka.cba9driver.data.CountryCode
import com.ingonoka.cba9driver.response.ChannelValueResponseData
import com.ingonoka.cba9driver.response.GenericResponseCode
import com.ingonoka.cba9driver.util.IntBuffer
import com.ingonoka.usbmanager.IUsbTransceiver

/**
 * Returns channel value data for a banknote validator. Note that this will differ depending on the protocol version
 * used/supported. For protocol versions greater or equal to 6:
 * ```
 *
 * byte offset      |  function
 * -------------------------------------------------------------
 * 0                | Generic Ok (0xF0)
 * 1                | Highest channel in set 1-16 (n)
 * 2 + n            | 3 byte for each ASCII country code in set
 * (2 + n) + (3*n)  | 4 byte value for each denomination
 * -------------------------------------------------------------
 *
 *```
 */
class ChannelValueData : SspCommand(SspCommandCode.ChannelValueData)

suspend fun IUsbTransceiver.getChannelValueData(
    protocolVersion: Int,
    fixedCountryCode: CountryCode = CountryCode.PHP,
    valueMultiplier: Int = 1
): Result<ChannelValueResponseData> = try {

    ChannelValueData().run(this).onSuccess {
        if (it.genericResponseCode != GenericResponseCode.OK)
            throw Exception("Failure response: ${it.genericResponseCode.convertToMetaCode(SspCommandCode.Hold)}")
    }.map {
        val bytes = IntBuffer.wrap(it.data)

        if (!bytes.hasBytesLeftToRead())
            throw Exception("Command to get channel value data returned no data")

        ChannelValueResponseData.decode(bytes, protocolVersion, fixedCountryCode, valueMultiplier).getOrThrow()
    }

} catch (e: Exception) {

    Result.failure(Exception("Failed execution of get channel value data.", e))
}
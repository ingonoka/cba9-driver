/*
 * Copyright (c) 2022. Ingo Noka
 * This file belongs to project load-kiosk.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driver.command

import com.ingonoka.cba9driver.response.GenericResponseCode
import com.ingonoka.cba9driver.response.RequestKeyExchangeResponseData
import com.ingonoka.cba9driver.util.listOfIntBuffer
import com.ingonoka.usbmanager.IUsbTransceiver

/**
 * The eight data bytes are a 64-bit number representing the Host intermediate key. If the Generator
 * and Modulus have been set the slave will calculate the reply with the generic response and eight data bytes
 * representing the slave intermediate key. The host and slave will then calculate the key.
 *
 * If Generator and Modulus are not set then the slave will reply FAIL.
 */
class RequestKeyExchange(key: List<Int>) : SspCommand(SspCommandCode.RequestKeyExchange, key)

suspend fun IUsbTransceiver.requestKeyExchange(keyData: List<Int>): Result<RequestKeyExchangeResponseData> = try {

    require(keyData.size == 8)

    RequestKeyExchange(keyData).run(this)
        .onSuccess {
            if (it.genericResponseCode != GenericResponseCode.OK)
                throw Exception("Failure response: ${it.genericResponseCode.convertToMetaCode(SspCommandCode.Hold)}")
        }.mapCatching {
            val buf = it.data.listOfIntBuffer()

            if (!buf.hasBytesLeftToRead())
                throw Exception("Command to request key exchange returned no data")

            RequestKeyExchangeResponseData.decode(buf).getOrThrow()
        }

} catch (e: Exception) {

    Result.failure(Exception("Failed execution of request key exchange command", e))
}
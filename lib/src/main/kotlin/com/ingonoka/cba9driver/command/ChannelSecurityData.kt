/*
 * Copyright (c) 2023. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driver.command

import com.ingonoka.cba9driver.response.ChannelSecurityResponseData
import com.ingonoka.cba9driver.response.GenericResponseCode
import com.ingonoka.cba9driver.util.listOfIntBuffer
import com.ingonoka.usbmanager.IUsbTransceiver

/**
 * Command which returns a number of channels byte (the highest channel used) and then 1 to n bytes which give the
 * security of each channel up to the highest one, a zero indicates that the channel is not implemented.
 */
class ChannelSecurityData : SspCommand(SspCommandCode.ChannelSecurityData)

suspend fun IUsbTransceiver.getChannelSecurityData(): Result<ChannelSecurityResponseData> = try {

    ChannelSecurityData().run(this)
        .onSuccess {
            if (it.genericResponseCode != GenericResponseCode.OK)
                throw Exception("Failure response: ${it.genericResponseCode.convertToMetaCode(SspCommandCode.Hold)}")
        }.mapCatching {
            val bytes = it.data.listOfIntBuffer()

            if (!bytes.hasBytesLeftToRead())
                throw Exception("Command to get channel security data returned no data")

            ChannelSecurityResponseData.decode(bytes).getOrThrow()
        }

} catch (e: Exception) {

    Result.failure(Exception("Failed execution of get channel security data command", e))
}
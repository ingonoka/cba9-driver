/*
 * Copyright (c) 2022. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driver.response

import com.ingonoka.cba9driver.data.ChannelSecurity
import com.ingonoka.cba9driver.util.IntBuffer
import com.ingonoka.cba9driver.util.ReadIntBuffer
import com.ingonoka.cba9driver.util.Stringifiable
import com.ingonoka.cba9driver.util.WriteIntBuffer

/**
 * Data returned by the CBA9 in response to a channel security request
 */
class ChannelSecurityResponseData(
    private val channelSecurities: List<ChannelSecurity>,
) : SspResponseData(), Stringifiable {

    fun encode(buf: WriteIntBuffer = IntBuffer.empty()): Result<WriteIntBuffer> = try {

        if (channelSecurities.isNotEmpty()) {
            val codes = channelSecurities.map { it.code }
            buf.writeByte(channelSecurities.size)
            buf.write(codes)
        }

        Result.success(buf)

    } catch (e: Exception) {

        Result.failure(e)
    }

    companion object {

        internal fun decode(bytes: List<Int>): Result<ChannelSecurityResponseData> = decode(IntBuffer.wrap(bytes))

        internal fun decode(buf: ReadIntBuffer): Result<ChannelSecurityResponseData> = try {

            val maxChannelNumber = buf.readByte().getOrThrow()

            val listOfChannelSecurities = List(maxChannelNumber) {
                ChannelSecurity.build(buf).getOrThrow()
            }

            Result.success(ChannelSecurityResponseData(listOfChannelSecurities))

        } catch (e: Exception) {

            Result.failure(Exception("Failed creation of channel security data", e))
        }
    }

    override fun toString(): String = stringify()

    override fun stringify(short: Boolean, indent: String): String =
        if (short) {
            channelSecurities.mapIndexed { index, channelSecurity ->
                "$index/${channelSecurity.name}"
            }.joinToString()

        } else {
            channelSecurities.mapIndexed { index, channelSecurity ->
                "$index:    ${channelSecurity.longName}"
            }.joinToString("\n")
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ChannelSecurityResponseData) return false

        if (channelSecurities != other.channelSecurities) return false

        return true
    }

    override fun hashCode(): Int {
        return channelSecurities.hashCode()
    }
}
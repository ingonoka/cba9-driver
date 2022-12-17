/*
 * Copyright (c) 2022. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driver.data

import com.ingonoka.cba9driver.util.ReadIntBuffer
import com.ingonoka.cba9driver.util.WriteIntBuffer


/**
 * Enumeration of channel security codes.
 */
enum class ChannelSecurity(val code: Int, val longName: String) {
    NOT_IMPLEMENTED(0, "Channel not implemented"),
    LOW(1, "Security Low"),
    STD(2, "Security Standard"),
    HIGH(3, "Security High"),
    INHIBITED(4, "Inhibited");

    fun encode(buf: WriteIntBuffer): Result<WriteIntBuffer> = try {

        buf.writeByte(code)

        Result.success(buf)

    } catch (e: Exception) {

        Result.failure(e)
    }

    companion object {

        internal fun build(bytes: ReadIntBuffer): Result<ChannelSecurity> = try {

            val code = bytes.readByte().getOrThrow()

            val countryCode = channelSecurityForCode(code).getOrThrow()

            Result.success(countryCode)

        } catch (e: Exception) {

            Result.failure(Exception("Failed creation of channel security", e))
        }

        private fun channelSecurityForCode(code: Int): Result<ChannelSecurity> = try {
            val channelSecurity = values().find {
                it.code == code
            } ?: throw Exception("Unknown channel security code: $code")

            Result.success(channelSecurity)

        } catch (e: Exception) {
            Result.failure(Exception("Failed to create channel security code", e))

        }
    }
}
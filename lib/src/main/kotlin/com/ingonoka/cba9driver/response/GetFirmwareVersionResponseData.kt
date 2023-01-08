/*
 * Copyright (c) 2023. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driver.response

import com.ingonoka.cba9driver.util.IntBuffer
import com.ingonoka.cba9driver.util.ReadIntBuffer
import com.ingonoka.cba9driver.util.Stringifiable
import com.ingonoka.cba9driver.util.WriteIntBuffer

/**
 * Data returned by the CBA9 in response to a get firmware command
 */
class GetFirmwareVersionResponseData(
    val firmwareVersion: String
) : SspResponseData(), Stringifiable {

    fun encode(buf: WriteIntBuffer = IntBuffer.empty()): Result<WriteIntBuffer> = try {

        buf.write(firmwareVersion)

        Result.success(buf)

    } catch (e: Exception) {

        Result.failure(e)
    }

    companion object {

        internal fun decode(data: List<Int>, n: Int = data.size): Result<GetFirmwareVersionResponseData> = decode(IntBuffer.wrap(data), n)

        /**
         * The builder assumes that all bytes of [bytes] represent the version string in UTF8 encoding
         */
        internal fun decode(bytes: ReadIntBuffer, n: Int = bytes.bytesLeftToRead()): Result<GetFirmwareVersionResponseData> = try {

            val firmwareVersion = bytes.readString(n).getOrThrow()

            Result.success(GetFirmwareVersionResponseData(firmwareVersion))

        } catch (e: Exception) {

            Result.failure(Exception("Failed creation of firmware version data", e))
        }
    }

    override fun stringify(short: Boolean, indent: String): String =
        if (short) {
            "fw:$firmwareVersion"
        } else {
            "Firmware Version:     $firmwareVersion)"
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GetFirmwareVersionResponseData) return false

        if (firmwareVersion != other.firmwareVersion) return false

        return true
    }

    override fun hashCode(): Int {
        return firmwareVersion.hashCode()
    }
}
/*
 * Copyright (c) 2023. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driver.response

import com.ingonoka.cba9driver.hexutils.toHexChunked
import com.ingonoka.cba9driver.hexutils.toHexShortShort
import com.ingonoka.cba9driver.util.IntBuffer
import com.ingonoka.cba9driver.util.ReadIntBuffer
import com.ingonoka.cba9driver.util.Stringifiable
import com.ingonoka.cba9driver.util.WriteIntBuffer

/**
 * Represents an SSP response to an SSP command sent to the device.
 */
class SspResponse(
    val genericResponseCode: GenericResponseCode,
    val data: List<Int> = listOf()
) : Stringifiable {

    fun encode(buf: WriteIntBuffer = IntBuffer.empty()): Result<WriteIntBuffer> = try {

        genericResponseCode.encode(buf).getOrThrow()
        buf.write(data)

        Result.success(buf)

    } catch (e: Exception) {

        Result.failure(e)
    }

    /**
     * Used to create a [SspResponse] from a string of bytes received from a CBA9 device.
     *
     * code received from the device will be converted into a meta response code that identifies this meaning.
     */
    companion object {

        internal fun decode(bytes: List<Int>): Result<SspResponse> = decode(IntBuffer.wrap(bytes))

        internal fun decode(bytes: ReadIntBuffer): Result<SspResponse> = try {

            val genericResponseCode = GenericResponseCode.decode(bytes).getOrThrow()

            val sspResponse = if (bytes.hasBytesLeftToRead())
                SspResponse(genericResponseCode, bytes.readRemaining().getOrThrow())
            else
                SspResponse(genericResponseCode)

            Result.success(sspResponse)

        } catch (e: Exception) {
            Result.failure(Exception("Failed building SSP Response", e))
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SspResponse

        if (genericResponseCode != other.genericResponseCode) return false
        if (data != other.data) return false

        return true
    }

    override fun hashCode(): Int {
        var result = genericResponseCode.hashCode()
        result = 31 * result + data.hashCode()
        return result
    }

    override fun toString(): String = stringify()

    override fun stringify(short: Boolean, indent: String): String {

        return if (short) {
            "$genericResponseCode ${if (data.isNotEmpty()) ": ${data.toHexShortShort()}" else ""}"
        } else {
            """
            |
            |Response:  $genericResponseCode
            |${data.toHexChunked()}
            """.trimMargin()
        }
    }

}
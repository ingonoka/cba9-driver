/*
 * Copyright (c) 2022. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driver.response

import com.ingonoka.cba9driver.data.SerialNumber
import com.ingonoka.cba9driver.util.IntBuffer
import com.ingonoka.cba9driver.util.ReadIntBuffer
import com.ingonoka.cba9driver.util.Stringifiable
import com.ingonoka.cba9driver.util.WriteIntBuffer

/**
 * Data returned by the CBA9 in response to a get serial number command.
 *
 * The serial number is an ASCII string.
 */
class GetSerialNumberResponseData(
    val serialNumber: SerialNumber
) : SspResponseData(), Stringifiable {

    fun encode(buf: WriteIntBuffer = IntBuffer.empty()): Result<WriteIntBuffer> = try {

        serialNumber.encode(buf).getOrThrow()

        Result.success(buf)

    } catch (e: Exception) {

        Result.failure(e)
    }

    companion object {

        internal fun decode(data: List<Int>): Result<GetSerialNumberResponseData> = decode(IntBuffer.wrap(data))

        internal fun decode(bytes: ReadIntBuffer): Result<GetSerialNumberResponseData> = try {

            val serialNumber = SerialNumber.decode(bytes).getOrThrow()

            Result.success(GetSerialNumberResponseData(serialNumber))

        } catch (e: Exception) {

            Result.failure(Exception("Failed creation of serial number data", e))
        }
    }

    override fun stringify(short: Boolean, indent: String): String =
        if (short) {
            indent + "sn:${serialNumber.stringify(true, "")}"
        } else {
            indent + "Serial Number: ${serialNumber.stringify(false, "")}"
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GetSerialNumberResponseData) return false

        if (serialNumber != other.serialNumber) return false

        return true
    }

    override fun hashCode(): Int {
        return serialNumber.hashCode()
    }
}


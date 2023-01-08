/*
 * Copyright (c) 2023. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driver.data

import com.ingonoka.cba9driver.util.IntBuffer
import com.ingonoka.cba9driver.util.ReadIntBuffer
import com.ingonoka.cba9driver.util.Stringifiable
import com.ingonoka.cba9driver.util.WriteIntBuffer

/**
 * Class holding the serial number of the device (as returned to a serial number command).
 *
 * Instead of using a vanilla integer for serial numbers, this class is used to make reading code easier.
 */
@JvmInline
value class SerialNumber(val value: Long = 0) : Stringifiable {

    fun encode(buf: WriteIntBuffer): Result<WriteIntBuffer> = try {

        buf.write(value, 4)

        Result.success(buf)

    } catch (e: Exception) {

        Result.failure(e)
    }

    companion object {

        internal fun decode(data: List<Int>): Result<SerialNumber> = decode(IntBuffer.wrap(data))

        internal fun decode(bytes: ReadIntBuffer): Result<SerialNumber> = try {

            val serialNumber = bytes
                .readLong(4)
                .getOrThrow()

            Result.success(SerialNumber(serialNumber))

        } catch (e: Exception) {

            Result.failure(Exception("Failed creation of serial number", e))
        }
    }

    override fun stringify(short: Boolean, indent: String): String =
        if (short) {
            "%04d".format(value)
        } else {
            "%04d (%08x)".format(value, value)
        }
}
/*
 * Copyright (c) 2022. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driver.data

import com.ingonoka.cba9driver.util.ByteOrder
import com.ingonoka.cba9driver.util.ReadIntBuffer
import com.ingonoka.cba9driver.util.WriteIntBuffer

/**
 * Class holding the multiplier to be used to calculate the actual value of a banknote. For example if
 * the multiplier is 10 and the banknote value is 5, then the actual banknote value is 50.  This is a workaround
 * for devices that support banknotes with a value bigger than 255, as the banknote value is expressed in a single
 * byte. For devices with protocol version higher than 6, the banknote values are expressed in 3 bytes and the
 * multiplier is not required.
 */
@JvmInline
value class ValueMultiplier(val value: Int) {

    fun encode(buf: WriteIntBuffer, byteOrder: ByteOrder): Result<WriteIntBuffer> = try {

        buf.write(value, 3, byteOrder)

        Result.success(buf)

    } catch (e: Exception) {

        Result.failure(e)
    }

    companion object {

        internal fun build(bytes: ReadIntBuffer, byteOrder: ByteOrder): Result<ValueMultiplier> = try {

            val valueMultiplier = bytes.readInt(3, byteOrder).getOrThrow()

            Result.success(ValueMultiplier(valueMultiplier))

        } catch (e: Exception) {

            Result.failure(Exception("Failed creation of value multiplier", e))
        }
    }
}
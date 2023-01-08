/*
 * Copyright (c) 2023. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driver.response

import com.ingonoka.cba9driver.hexutils.toHexShortShort
import com.ingonoka.cba9driver.util.*

/**
 * Data returned by the CBA9 in response to a key exchange request command.
 */
class KeyExchangeResponseData(
    val slaveIntermediateKey: Long
) : SspResponseData(), Stringifiable {

    fun encode(buf: WriteIntBuffer = IntBuffer.empty()): Result<WriteIntBuffer> = try {

        buf.write(slaveIntermediateKey, 8, ByteOrder.LITTLE_ENDIAN)

        Result.success(buf)

    } catch (e: Exception) {

        Result.failure(e)
    }

    companion object {

        internal fun decode(data: List<Int>): Result<KeyExchangeResponseData> = decode(IntBuffer.wrap(data))

        internal fun decode(buf: ReadIntBuffer): Result<KeyExchangeResponseData> = try {

            val slaveIntermediateKey = buf.readLong(8, ByteOrder.LITTLE_ENDIAN).getOrThrow()

            Result.success(KeyExchangeResponseData(slaveIntermediateKey))

        } catch (e: Exception) {

            Result.failure(Exception("Failed creation of key exchange slave intermediate key", e))
        }
    }

    override fun stringify(short: Boolean, indent: String): String =
        if (short) {
            "key:${slaveIntermediateKey.toByteArrayWithoutLeadingZeros().getOrThrow().toHexShortShort()}"
        } else {
            "Slave Intermediate Key:     ${slaveIntermediateKey.toByteArrayWithoutLeadingZeros().getOrThrow().toHexShortShort()}"
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is KeyExchangeResponseData) return false

        if (slaveIntermediateKey != other.slaveIntermediateKey) return false

        return true
    }

    override fun hashCode(): Int {
        return slaveIntermediateKey.hashCode()
    }
}
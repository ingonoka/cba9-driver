/*
 * Copyright (c) 2023. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driver.response

import com.ingonoka.cba9driver.util.*

/**
 * Data returned by the CBA9 in response to a get counters command.
 *
 */
class GetCountersResponseData(
    val stacked: Long,
    val stored: Long,
    val dispensed: Long,
    val transferredToStack: Long,
    val rejected: Long
) : SspResponseData(), Stringifiable {

    fun encode(buf: WriteIntBuffer = IntBuffer.empty(21)): Result<WriteIntBuffer> = try {

        buf.writeByte(5)
        buf.write(stacked, 4, ByteOrder.LITTLE_ENDIAN)
        buf.write(stored, 4, ByteOrder.LITTLE_ENDIAN)
        buf.write(dispensed, 4, ByteOrder.LITTLE_ENDIAN)
        buf.write(transferredToStack, 4, ByteOrder.LITTLE_ENDIAN)
        buf.write(rejected, 4, ByteOrder.LITTLE_ENDIAN)

        Result.success(buf)

    } catch (e: Exception) {

        Result.failure(e)
    }

    companion object {

        fun decode(data: List<Int>): Result<GetCountersResponseData> = decode(IntBuffer.wrap(data))

        fun decode(bytes: ReadIntBuffer): Result<GetCountersResponseData> = try {

            val numCounters = bytes.readByte().getOrThrow()

            if (numCounters != 5) throw Exception("Number of counters should be 5: is $numCounters")

            val stacked = bytes.readLong(4, ByteOrder.LITTLE_ENDIAN).getOrThrow()
            val stored = bytes.readLong(4, ByteOrder.LITTLE_ENDIAN).getOrThrow()
            val dispensed = bytes.readLong(4, ByteOrder.LITTLE_ENDIAN).getOrThrow()
            val transferredToStack = bytes.readLong(4, ByteOrder.LITTLE_ENDIAN).getOrThrow()
            val rejected = bytes.readLong(4, ByteOrder.LITTLE_ENDIAN).getOrThrow()

            Result.success(GetCountersResponseData(stacked, stored, dispensed, transferredToStack, rejected))

        } catch (e: Exception) {

            Result.failure(Exception("Failed creation of get counter response data", e))
        }
    }

    override fun stringify(short: Boolean, indent: String): String =
        if (short) {
            "stacked:$stacked,stored:$stored,dispensed:$dispensed,toStack:$transferredToStack,rejected:$rejected"
        } else {
            """
            |Stacked:               $stacked
            |Stored:                $stored
            |Dispensed:             $dispensed
            |Transferred to Stack:  $transferredToStack
            |Rejected:              $rejected
            """.trimMargin()
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GetCountersResponseData) return false

        if (stacked != other.stacked) return false
        if (stored != other.stored) return false
        if (dispensed != other.dispensed) return false
        if (transferredToStack != other.transferredToStack) return false
        if (rejected != other.rejected) return false

        return true
    }

    override fun hashCode(): Int {
        var result = stacked.hashCode()
        result = 31 * result + stored.hashCode()
        result = 31 * result + dispensed.hashCode()
        result = 31 * result + transferredToStack.hashCode()
        result = 31 * result + rejected.hashCode()
        return result
    }

}
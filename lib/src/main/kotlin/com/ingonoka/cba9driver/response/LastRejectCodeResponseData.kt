/*
 * Copyright (c) 2023. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driver.response

import com.ingonoka.cba9driver.data.RejectCode
import com.ingonoka.cba9driver.util.IntBuffer
import com.ingonoka.cba9driver.util.ReadIntBuffer
import com.ingonoka.cba9driver.util.Stringifiable
import com.ingonoka.cba9driver.util.WriteIntBuffer

/**
 * Data returned by the CBA9 in response to a get last reject code command
 */
class LastRejectCodeResponseData(
    val rejectCode: RejectCode
) : SspResponseData(), Stringifiable {

    fun encode(buf: WriteIntBuffer = IntBuffer.empty()): Result<WriteIntBuffer> = try {

        rejectCode.encode(buf).getOrThrow()

        Result.success(buf)

    } catch (e: Exception) {

        Result.failure(e)
    }

    companion object {

        internal fun decode(data: List<Int>): Result<LastRejectCodeResponseData> = decode(IntBuffer.wrap(data))

        internal fun decode(bytes: ReadIntBuffer): Result<LastRejectCodeResponseData> = try {

            val rejectCode = RejectCode.build(bytes).getOrThrow()

            Result.success(LastRejectCodeResponseData(rejectCode))

        } catch (e: Exception) {

            Result.failure(Exception("Failed creation of last reject code data", e))
        }
    }

    override fun stringify(short: Boolean, indent: String): String = rejectCode.stringify(short)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LastRejectCodeResponseData) return false

        if (rejectCode != other.rejectCode) return false

        return true
    }

    override fun hashCode(): Int {
        return rejectCode.hashCode()
    }

}
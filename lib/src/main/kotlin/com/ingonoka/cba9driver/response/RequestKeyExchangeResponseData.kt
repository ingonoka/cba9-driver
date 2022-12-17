/*
 * Copyright (c) 2022. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driver.response

import com.ingonoka.cba9driver.util.IntBuffer
import com.ingonoka.cba9driver.util.ReadIntBuffer
import com.ingonoka.cba9driver.util.WriteIntBuffer

class RequestKeyExchangeResponseData(
    val bnaInterimKeyData: List<Int>
) {

    fun encode(buf: WriteIntBuffer = IntBuffer.empty()): Result<WriteIntBuffer> = try {

        buf.write(bnaInterimKeyData.reversed())

        Result.success(buf)

    } catch (e: Exception) {

        Result.failure(e)
    }

    companion object {

        internal fun decode(bytes: List<Int>): Result<RequestKeyExchangeResponseData> = decode(IntBuffer.wrap(bytes))

        internal fun decode(buf: ReadIntBuffer): Result<RequestKeyExchangeResponseData> = try {

            val keyData = buf.readList(8).onSuccess { it.reversed() }.getOrThrow()

            Result.success(RequestKeyExchangeResponseData(keyData))

        } catch (e: Exception) {

            Result.failure(Exception("Failed creation of request key exchange response data", e))
        }
    }

}
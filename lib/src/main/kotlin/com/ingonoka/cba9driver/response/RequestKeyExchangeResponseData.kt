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
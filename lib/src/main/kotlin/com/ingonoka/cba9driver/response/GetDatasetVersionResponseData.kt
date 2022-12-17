/*
 * Copyright (c) 2022. Ingo Noka
 * This file belongs to project load-kiosk.
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
 * Data returned by the CBA9 in response to a get dataset version request command.
 */
class GetDatasetVersionResponseData(
    val datasetVersion: String = ""
) : SspResponseData(), Stringifiable {

    fun encode(buf: WriteIntBuffer = IntBuffer.empty()): Result<WriteIntBuffer> = try {

        buf.write(datasetVersion)

        Result.success(buf)

    } catch (e: Exception) {

        Result.failure(e)
    }

    companion object {

        internal fun decode(data: List<Int>, n: Int = data.size): Result<GetDatasetVersionResponseData> = decode(IntBuffer.wrap(data), n)

        internal fun decode(buf: ReadIntBuffer, n: Int = buf.bytesLeftToRead()): Result<GetDatasetVersionResponseData> = try {

            val datasetVersion = buf.readString(n).getOrThrow()

            Result.success(GetDatasetVersionResponseData(datasetVersion))

        } catch (e: Exception) {

            Result.failure(Exception("Failed creation of firmware version data", e))
        }
    }

    override fun stringify(short: Boolean, indent: String): String =
        if (short) {
            datasetVersion
        } else {
            "Dataset Version: $datasetVersion"
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GetDatasetVersionResponseData) return false

        if (datasetVersion != other.datasetVersion) return false

        return true
    }

    override fun hashCode(): Int {
        return datasetVersion.hashCode()
    }
}
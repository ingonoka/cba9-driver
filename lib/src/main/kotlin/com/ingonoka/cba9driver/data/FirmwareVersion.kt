/*
 * Copyright (c) 2023. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driver.data

import com.ingonoka.cba9driver.util.ReadIntBuffer
import com.ingonoka.cba9driver.util.Stringifiable
import com.ingonoka.cba9driver.util.WriteIntBuffer

/**
 * Class holding the firmware version of the device (as returned to a setup command)
 */
@JvmInline
value class FirmwareVersion(val firmwareVersion: String = "") : Stringifiable {

    fun encode(buf: WriteIntBuffer): Result<WriteIntBuffer> = try {

        val asFloat = firmwareVersion.toFloat() * 100
        buf.write("%04.0f".format(asFloat))

        Result.success(buf)

    } catch (e: Exception) {

        Result.failure(e)
    }

    companion object {

        internal fun build(bytes: ReadIntBuffer): Result<FirmwareVersion> = try {

            val firmwareVersion = bytes
                .readString(4)
                .map { it.toInt() / 100.0 }
                .getOrThrow()

            Result.success(FirmwareVersion("%.2f".format(firmwareVersion)))

        } catch (e: Exception) {
            Result.failure(Exception("Failed creation of firmware version", e))
        }
    }

    override fun toString() = stringify(true, "")

    override fun stringify(short: Boolean, indent: String): String =
        if (short) firmwareVersion else "FW Version: $firmwareVersion"

}
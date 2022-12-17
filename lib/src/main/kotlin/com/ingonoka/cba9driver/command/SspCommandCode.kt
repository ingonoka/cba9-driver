/*
 * Copyright (c) 2022. Ingo Noka
 * This file belongs to project load-kiosk.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driver.command

import com.ingonoka.cba9driver.hexutils.toHex
import com.ingonoka.cba9driver.util.ReadIntBuffer
import com.ingonoka.cba9driver.util.Stringifiable
import com.ingonoka.cba9driver.util.WriteIntBuffer

/**
 * Command code included in the payload of a command protocol message sent to the device. See the class implementing
 * the command for details.
 */
enum class SspCommandCode(val commandCode: Int) : Stringifiable {

    Unknown(0),
    Sync(0x11),
    Reset(0x01),
    HostProtocolVersion(0x06),
    Poll(0x07),
    GetSerialNumber(0x0C),
    Disable(0x09),
    Enable(0x0A),
    GetFirmwareVersion(0x20),
    GetDatasetVersion(0x21),
    SetInhibits(0x02),
    DisplayOn(0x03),
    DisplayOff(0x04),
    Reject(0x08),
    UnitData(0x0D),
    ChannelValueData(0x0E),
    ChannelSecurityData(0x0F),
    LastRejectCode(0x17),
    PollWithAck(0x56),
    EventAck(0x57),
    GetCounters(0x58),
    ResetCounters(0x59),
    SetGenerator(0x4A),
    SetModulus(0x4B),
    RequestKeyExchange(0x4C),
    SspSetEncryptionKey(0x60),
    SspEncryptionResetToDefault(0x61),
    Hold(0x18),
    SetupRequest(0x05);

    fun encode(buf: WriteIntBuffer): Result<WriteIntBuffer> = try {

        buf.writeByte(commandCode)

        Result.success(buf)

    } catch (e: Exception) {

        Result.failure(e)
    }

    override fun toString(): String = stringify()

    override fun stringify(short: Boolean, indent: String): String = if (short) {
        indent + "$name (${commandCode.toByte().toHex()})"
    } else {
        indent + "$name (${commandCode.toByte().toHex()})"
    }

    companion object {

        internal fun decode(bytes: ReadIntBuffer): Result<SspCommandCode> = try {

            val code = bytes.readByte().getOrThrow()

            commandCodeForCode(code)

        } catch (e: Exception) {

            Result.failure(e)
        }

        private fun commandCodeForCode(code: Int): Result<SspCommandCode> = try {
            val commandCode = SspCommandCode.values().find {
                it.commandCode == code
            } ?: throw Exception("Failed to find command code for code: $code")

            Result.success(commandCode)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }
}
/*
 * Copyright (c) 2022. Ingo Noka
 * This file belongs to project load-kiosk.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driver.response

import com.ingonoka.cba9driver.command.SspCommandCode
import com.ingonoka.cba9driver.hexutils.toHex
import com.ingonoka.cba9driver.util.IntBuffer
import com.ingonoka.cba9driver.util.ReadIntBuffer
import com.ingonoka.cba9driver.util.Stringifiable
import com.ingonoka.cba9driver.util.WriteIntBuffer

/**
 * The generic response codes defined for a CBA9.  Also includes meta response codes that identify special
 * meaning of generic response codes for some commands.
 *
 * The code used here is just the lower nibble of the actual code, for example `2` instead of `0xF2` for
 * command unknown.
 *
 * Meta codes are 256 and larger, i.e. don't fit into one byte and therefore do not overlap with actual response codes
 * returned from the device.
 */
enum class GenericResponseCode(val code: Int) : Stringifiable {
    /**
     * Meta code for commands that have failed or have not been executed yet
     */
    UNKNOWN(-1),

    /**
     * Command was executed successful. The meaning is command specific.
     */
    OK(0xF0.toByte().toInt()),

    /**
     * The command code included sent to the device is unknown.
     */
    COMMAND_NOT_KNOWN(0xF2.toByte().toInt()),

    /**
     * A command was received by a peripheral, but an incorrect number of parameters were received.
     */
    WRONG_NUMBER_OF_PARAMETERS(0xF3.toByte().toInt()),

    /**
     * One of the parameters sent with a command is out of range.
     */
    PARAMETERS_OUT_OF_RANGE(0xF4.toByte().toInt()),

    /**
     * A command sent could not be processed at that time. E.g. sending a dispense command before the last dispense
     * operation has completed.
     */
    COMMAND_CANNOT_BE_PROCESSED(0xF5.toByte().toInt()),

    /**
     * Reported for errors in the execution of software e.g. Divide by zero. This may also be reported if there
     * is a problem resulting from a failed remote firmware upgrade, in this case the firmware upgrade should be redone.
     */
    SOFTWARE_ERROR(0xF6.toByte().toInt()),

    /**
     * Command failure
     */
    FAIL(0xF8.toByte().toInt()),

    /**
     * The slave is in encrypted communication mode but the encryption keys have not been negotiated.
     */
    KEY_NOT_SET(0xFA.toByte().toInt()),

    /**
     * Meta. Set if Reject command returns COMMAND_CANNOT_BE_PROCESSED
     */
    NOTE_NOT_IN_ESCROW(0x100),

    /**
     * Meta. Set for generator and modulus if the device returns PARAMETERS_OUT_OF_RANGE which means the numbers
     * are not prime
     */
    NOT_PRIME(0x101),

    /**
     * Meta. Set for EventAck command. If no event currently requires acknowledgement a COMMAND_CANNOT_BE_PROCESSED
     * response will be given and converted to this meta response code.
     */
    NO_COMMAND_TO_ACKNOWLEDGE(0x102),

    /**
     * Meta. Set if host protocol command returns fail. If the device supports the requested protocol OK (0xF0)
     * will be returned. If not then FAIL (0xF8) will be returned
     */
    PROTOCOL_NOT_SUPPORTED(0x103);

    fun encode(
        buf: WriteIntBuffer = IntBuffer.empty()
    ): Result<WriteIntBuffer> = try {

        buf.writeByte(code)

        Result.success(buf)

    } catch (e: Exception) {

        Result.failure(e)
    }

    companion object {

        internal fun decode(bytes: List<Int>): Result<GenericResponseCode> =
            decode(IntBuffer.wrap(bytes))

        internal fun decode(bytes: ReadIntBuffer): Result<GenericResponseCode> = try {

            val code = bytes.readByte().getOrThrow()

            val genericResponseCode = values().find {
                it.code == code
            } ?: throw Exception("Unknown response code")

            Result.success(genericResponseCode)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    /**
     * For some commands the generic response code has a special meaning.  This function converts
     * the generic command code to a more specific meta code
     */
    internal fun convertToMetaCode(cmdCode: SspCommandCode): GenericResponseCode =
        if (this == OK) {
            OK
        } else when (cmdCode) {
            SspCommandCode.Reject -> if (this == COMMAND_CANNOT_BE_PROCESSED) NOTE_NOT_IN_ESCROW else this
            SspCommandCode.SetGenerator -> if (this == PARAMETERS_OUT_OF_RANGE) NOT_PRIME else this
            SspCommandCode.SetModulus -> if (this == PARAMETERS_OUT_OF_RANGE) NOT_PRIME else this
            SspCommandCode.Hold -> if (this == COMMAND_CANNOT_BE_PROCESSED) NOTE_NOT_IN_ESCROW else this
            SspCommandCode.EventAck -> if (this == COMMAND_CANNOT_BE_PROCESSED) NO_COMMAND_TO_ACKNOWLEDGE else this
            SspCommandCode.HostProtocolVersion -> if (this == FAIL) PROTOCOL_NOT_SUPPORTED else this
            else -> this
        }

    /**
     * For some commands the generic response code has a special meaning.  This function converts
     * the generic command code to a more specific meta code
     */
    internal fun convertFromMetaCode(cmdCode: SspCommandCode): GenericResponseCode =
        if (this.code < 255) {
            this
        } else when (cmdCode) {
            SspCommandCode.Reject -> if (this == NOTE_NOT_IN_ESCROW) COMMAND_CANNOT_BE_PROCESSED else this
            SspCommandCode.SetGenerator -> if (this == NOT_PRIME) PARAMETERS_OUT_OF_RANGE else this
            SspCommandCode.SetModulus -> if (this == NOT_PRIME) PARAMETERS_OUT_OF_RANGE else this
            SspCommandCode.Hold -> if (this == NOTE_NOT_IN_ESCROW) COMMAND_CANNOT_BE_PROCESSED else this
            SspCommandCode.EventAck -> if (this == NO_COMMAND_TO_ACKNOWLEDGE) COMMAND_CANNOT_BE_PROCESSED else this
            SspCommandCode.HostProtocolVersion -> if (this == PROTOCOL_NOT_SUPPORTED) FAIL else this
            else -> this
        }

    override fun toString(): String = stringify()

    override fun stringify(short: Boolean, indent: String): String = if (short) {
        "$indent$name (${code.toByte().toHex()})"
    } else {
        """|
            |${indent}Response:     $name
            |${indent}  Code:       $code
        """.trimMargin()
    }
}

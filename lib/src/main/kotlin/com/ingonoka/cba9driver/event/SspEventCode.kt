/*
 * Copyright (c) 2022. Ingo Noka
 * This file belongs to project load-kiosk.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driver.event

import com.ingonoka.cba9driver.util.ReadIntBuffer
import com.ingonoka.cba9driver.util.WriteIntBuffer

enum class SspEventCode(val eventCode: Int) {

    SlaveReset(0xF1.toByte().toInt()),
    Read(0xEF.toByte().toInt()),
    NoteCredit(0xEE.toByte().toInt()),
    Rejecting(0xED.toByte().toInt()),
    Rejected(0xEC.toByte().toInt()),
    Stacking(0xCC.toByte().toInt()),
    Stacked(0xEB.toByte().toInt()),
    UnsafeJam(0xE9.toByte().toInt()),
    Disabled(0xE8.toByte().toInt()),
    FraudAttempt(0xE6.toByte().toInt()),
    StackerFull(0xE7.toByte().toInt()),
    NoteClearedFromFront(0xE1.toByte().toInt()),
    NoteClearedIntoCashbox(0xE2.toByte().toInt()),
    ChannelDisable(0xB5.toByte().toInt()),
    Initialising(0xB6.toByte().toInt()),

    // META Events (these events are not provided in response to a poll command.  Instead these events
    // are generated by the driver in response to changes in the state of the validator, which usually
    // happens after a particular command was successfully executed

    Synced(0x01),
    Scanning(0x02),
    ChannelEnabled(0x03),
    AcceptNote(0x04),
    RejectNote(0x05),
    Enabled(0x06),
    Alive(0x07),
    StartMaintenance(0x08),
    EndMaintenance(0x09);

    fun encode(buf: WriteIntBuffer): Result<WriteIntBuffer> = try {

        buf.writeByte(eventCode)

        Result.success(buf)

    } catch (e: Exception) {

        Result.failure(e)
    }

    companion object {

        internal fun build(bytes: ReadIntBuffer): Result<SspEventCode> = try {

            val code = bytes.readByte().getOrThrow()

            sspEventCodeFromCode(code)

        } catch (e: Exception) {

            Result.failure(e)
        }

        fun sspEventCodeFromCode(code: Int): Result<SspEventCode> = try {
            val sspEventCode = values().find {
                it.eventCode == code
            } ?: throw Exception("Failed to find SSP Event code for code: $code")

            Result.success(sspEventCode)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

}
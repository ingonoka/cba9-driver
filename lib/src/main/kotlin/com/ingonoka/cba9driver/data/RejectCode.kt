/*
 * Copyright (c) 2022. Ingo Noka
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
 * Enumeration of reject codes.
 *
 *  After a note has been rejected a one-byte code to determine the cause of the note reject.
 */
enum class RejectCode(val rejectCode: Int, val longName: String) : Stringifiable {
    NOTE_ACCEPTED(0x00, "NOTE ACCEPTED"),

    LENGTH_FAIL(0x01, "The banknote has been read but it's length registers over the max length parameter."),
    AVERAGE_FAIL(0x02, "AVERAGE FAIL - Internal validation failure - banknote not recognised."),
    COASTLINE_FAIL(0x03, "COASTLINE FAIL - Internal validation failure - banknote not recognised."),
    GRAPH_FAIL(0x04, "GRAPH FAIL - Internal validation failure - banknote not recognised."),
    BURIED_FAIL(0x05, "BURIED FAIL - Internal validation failure - banknote not recognised."),
    CHANNEL_INHIBIT(
        0x06,
        "CHANNEL INHIBIT This banknote has been inhibited for acceptance in the dataset configuration."
    ),
    SECOND_NOTE_DETECTED(
        0x07,
        "SECOND NOTE DETECTED A second banknote was inserted into the validator while the first one was still being transported through the banknote path."
    ),
    REJECT_BY_HOST(
        0x08,
        "REJECT BY HOST The host system issues a Reject command when this banknote was held in escrow."
    ),
    CROSS_CHANNEL_DETECTED(
        0x09,
        "CROSS CHANNEL DETECTED This bank note was identified as existing in two or more separate channel definitions in the dataset."
    ),
    REAR_SENSOR_ERROR(0x0A, "REAR SENSOR ERROR An inconsistency in a position sensor detection was seen"),
    NOTE_TOO_LONG(0x0B, "NOTE TOO LONG The banknote failed dataset length checks."),
    DISABLED_BY_HOST(
        0x0C,
        "DISABLED BY HOST The bank note was validated on a channel that has been inhibited for acceptance by the host system."
    ),
    SLOW_MECH_(0x0D, "SLOW MECH The internal mechanism was detected as moving too slowly for correct validation."),
    STRIM_ATTEMPT(0x0E, "STRIM ATTEMPT An attempt to fraud the system was detected."),
    FRAUD_CHANNEL(0x0F, "FRAUD CHANNEL Obselete response."),
    NO_NOTES_DETECTED(
        0x10,
        "NO NOTES DETECTED A banknote detection was initiated but no banknotes were seen at the validation section."
    ),
    PEAK_DETECT_FAIL(0x11, "PEAK DETECT FAIL Internal validation fail. Banknote not recognised."),
    TWISTED_NOTE_REJECT(0x12, "TWISTED NOTE REJECT Internal validation fail. Banknote not recognised."),
    ESCROW_TIME_OUT(
        0x13,
        "ESCROW TIME OUT A banknote held in escrow was rejected due to the host not communicating within the time-out period. The default timeout period is the same as the poll timeout i.e. 10 seconds."
    ),
    BAR_CODE_SCAN_FAIL(0x14, "BAR CODE SCAN FAIL Internal validation fail. Banknote not recognised."),
    NO_CAM_ACTIVATE(
        0x15,
        "NO CAM ACTIVATE A banknote did not reach the internal note path for validation during transport."
    ),
    SLOT_FAIL_1(0x16, "SLOT FAIL 1 Internal validation fail. Banknote not recognised."),
    SLOT_FAIL_2(0x17, "SLOT FAIL 2 Internal validation fail. Banknote not recognised."),
    LENS_OVERSAMPLE(0x18, "LENS OVERSAMPLE The banknote was transported faster than the system could sample the note."),
    WIDTH_DETECTION_FAIL(0x19, "WIDTH DETECTION FAIL The banknote failed a measurement test."),
    SHORT_NOTE_DETECT(
        0x1A,
        "SHORT NOTE DETECT The banknote measured length fell outside of the validation parameter for minimum length."
    ),
    PAYOUT_NOTE(
        0x1B,
        "PAYOUT NOTE The reject code cammand was issued after a note was payed out using a note payout device."
    ),
    DOUBLE_NOTE_DETECTED(
        0x1C,
        "DOUBLE NOTE DETECTED More than one banknote was detected as overlayed during note entry."
    ),
    UNABLE_TO_STACK(
        0x1D,
        "UNABLE TO STACK The bill was unable to reach it's correct stacking position during transport."
    ),
    CREDIT_CARD_DETECTED(0x1F, "CREDIT CARD DETECTED Devices applicable: NV9 Family tree");


    fun encode(buf: WriteIntBuffer): Result<WriteIntBuffer> = try {

        buf.writeByte(rejectCode)

        Result.success(buf)

    } catch (e: Exception) {

        Result.failure(e)
    }

    companion object {

        internal fun build(bytes: ReadIntBuffer): Result<RejectCode> = try {

            val code = bytes.readByte().getOrThrow()

            rejectCodeForCode(code)

        } catch (e: Exception) {

            Result.failure(e)
        }

        private fun rejectCodeForCode(code: Int): Result<RejectCode> = try {
            val rejectCode = values().find {
                it.rejectCode == code
            } ?: throw Exception("Failed to find reject code for code: $code")

            Result.success(rejectCode)

        } catch (e: Exception) {

            Result.failure(e)
        }

    }

    override fun stringify(short: Boolean, indent: String): String =
        if (short) {
            "Rejected: $name"
        } else {
            "Rejected: Code $rejectCode - $longName"
        }
}
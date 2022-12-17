/*
 * Copyright (c) 2022. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driver.response

import com.ingonoka.cba9driver.data.ChannelNumber
import com.ingonoka.cba9driver.data.Denominations
import com.ingonoka.cba9driver.event.*
import com.ingonoka.cba9driver.util.IntBuffer
import com.ingonoka.cba9driver.util.ReadIntBuffer
import com.ingonoka.cba9driver.util.Stringifiable
import com.ingonoka.cba9driver.util.WriteIntBuffer

/**
 * Data returned by the CBA9 in response to a poll command.
 *
 * The response data contains either none, a single or multiple events.  The event is identified by a single byte
 * [SspEventCode] and some events have additional data
 */
class PollResponseData(
    val eventList: List<SspEvent>
) : SspResponseData(), Stringifiable {

    fun encode(buf: WriteIntBuffer = IntBuffer.empty(eventList.size), denominations: Denominations): Result<WriteIntBuffer> = try {

        eventList.forEach {
            it.sspEventCode.encode(buf).getOrThrow()

            if (it.sspEventCode in eventWithDenomination) {

                val channelNumber = denominations.denominationChannel(it.denomination).getOrThrow()

                buf.writeByte(channelNumber.channelNumber)

            }
        }

        Result.success(buf)

    } catch (e: Exception) {

        Result.failure(e)
    }

    companion object {

        private val eventWithDenomination = listOf(
            SspEventCode.Read, SspEventCode.NoteCredit, SspEventCode.FraudAttempt, SspEventCode.NoteClearedFromFront,
            SspEventCode.NoteClearedIntoCashbox
        )

        internal fun decode(data: List<Int>, denominations: Denominations): Result<PollResponseData> =
            decode(IntBuffer.wrap(data), denominations)

        internal fun decode(buf: ReadIntBuffer, denominations: Denominations): Result<PollResponseData> = try {

            val events = mutableListOf<SspEvent>()

            if (!buf.hasBytesLeftToRead()) {
                events.add(Alive())
            } else {

                while (buf.hasBytesLeftToRead()) {
                    val nextByte = buf.readByte().getOrThrow()

                    val nextEventCode = SspEventCode.sspEventCodeFromCode(nextByte).getOrThrow()

                    val event = if (nextEventCode in eventWithDenomination) {
                        val denominationCode = if (buf.hasBytesLeftToRead()) {
                            buf.readByte().getOrThrow()
                        } else {
                            throw Exception("This event type should have a denomination byte.")
                        }

                        val denomination = denominations.channelDenomination(ChannelNumber(denominationCode))

                        when (nextEventCode) {
                            SspEventCode.Read -> if (denomination.isUnknown()) Scanning() else AcceptedNote(denomination)
                            SspEventCode.NoteCredit -> NoteCredit(denomination)
                            SspEventCode.FraudAttempt -> FraudAttempt(denomination)
                            SspEventCode.NoteClearedFromFront -> NoteClearedFromFront(denomination)
                            SspEventCode.NoteClearedIntoCashbox -> NoteClearedIntoCashbox(denomination)
                            else -> throw RuntimeException("Unknown event code: $nextEventCode")
                        }
                    } else {
                        when (nextEventCode) {
                            SspEventCode.SlaveReset -> SlaveReset()
                            SspEventCode.Rejecting -> Rejecting()
                            SspEventCode.Rejected -> Rejected()
                            SspEventCode.Stacking -> Stacking()
                            SspEventCode.Stacked -> Stacked()
                            SspEventCode.UnsafeJam -> UnsafeJam()
                            SspEventCode.Disabled -> Disabled()
                            SspEventCode.StackerFull -> StackerFull()
                            SspEventCode.ChannelDisable -> ChannelDisable()
                            SspEventCode.Initialising -> Initialising()
                            else -> throw RuntimeException("Unknown event code: $nextEventCode")
                        }
                    }

                    events.add(event)
                }
            }

            Result.success(PollResponseData(events.toList()))

        } catch (e: Exception) {

            Result.failure(Exception("Failed creation of poll response data", e))
        }
    }

    override fun stringify(short: Boolean, indent: String): String =
        if (short) {
            eventList.joinToString(",") { it.stringify(true) }
        } else {
            eventList.joinToString(",", "[", "]") { it.stringify(false) }
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PollResponseData) return false

        if (eventList != other.eventList) return false

        return true
    }

    override fun hashCode(): Int {
        return eventList.hashCode()
    }

}
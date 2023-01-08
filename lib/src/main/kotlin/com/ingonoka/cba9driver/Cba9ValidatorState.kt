/*
 * Copyright (c) 2023. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driver

import com.ingonoka.cba9driver.data.CountryCode
import com.ingonoka.cba9driver.data.Denomination
import com.ingonoka.cba9driver.util.Stringifiable
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

enum class Cba9ValidatorState {
    UNDEFINED,

    DISCONNECTED,

    FAILURE,

    /**
     * Validator is initializing (anything that happens after the sync), which sets the status to PRESENT and
     * the enabled command. Initialization includes, negotiating encryption, setting protocol version, channel inhibits etc.
     */
    INITIALIZING,

    /**
     * Validator is in the process of scanning a banknote
     */
    SCANNING,

    /**
     * A banknote is in escrow
     */
    NOTE_IN_ESCROW,

    /**
     * Validator is in the process of returning the banknote to the customer
     */
    REJECTING,

    /**
     * Validator is in the process of stacking a banknote into the cashbox
     */
    STACKING,

    /**
     * Validator is in the process of stacking a banknote into the cashbox.  The banknote is now in a
     * position from which it cannot be retrieved from the front of the validator anymore.
     */
    STACKING_CREDITED,

    /**
     * Validator is ready to accept a banknote.  At least one channel is not inhibited
     */
    READY,

    /**
     * Validator is jammed and the note can potentially be retrieved from the front.  The next event would likely be a
     * disabled by slave.
     */
    UNSAFE_JAM,

    /**
     * Validator is disabled (usually after a disable command)
     */
    DISABLED,

    /**
     * Validator is present and enabled but all channels are inhibited
     */
    INHIBITED,

    /**
     * Validator created a cashbox full event.  Subsequent POLL commands will likely yield the same event again and again
     * until the cashbox was emptied and the validator restarted and initialized again.
     */
    CASHBOX_FULL
}

/**
 * State of the CBA9 banknote validator.
 *
 *
 * Some states carry a banknote denomination to indicate which banknote is processed.
 */
class Cba9ValidatorStateHolder(
    val timeOfStateChange: Instant,
    val state: Cba9ValidatorState,
    val denomination: Denomination = Denomination(0, CountryCode.UNKNOWN)
) : Stringifiable {

    override fun stringify(short: Boolean, indent: String): String =
        if (short) {
            indent + "${state.name} ${if (denomination.isUnknown()) "" else denomination.stringify(true)}"
        } else {
            val timeStr = if (timeOfStateChange != Instant.DISTANT_PAST) {
                timeOfStateChange.toLocalDateTime(TimeZone.of("+08:00"))
            } else {
                ""
            }

            "Time of state change (local): $timeStr, State: ${state.name} ${
                if (denomination.isUnknown()) "" else "Banknote: ${
                    denomination.stringify(
                        false
                    )
                }"
            }"
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Cba9ValidatorStateHolder) return false

        if (timeOfStateChange != other.timeOfStateChange) return false
        if (state != other.state) return false
        if (denomination != other.denomination) return false

        return true
    }

    override fun hashCode(): Int {
        var result = timeOfStateChange.hashCode()
        result = 31 * result + state.hashCode()
        result = 31 * result + denomination.hashCode()
        return result
    }
}
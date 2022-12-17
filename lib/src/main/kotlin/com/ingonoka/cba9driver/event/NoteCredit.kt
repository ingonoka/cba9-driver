/*
 * Copyright (c) 2022. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driver.event

import com.ingonoka.cba9driver.data.Denomination

/**
 * This event is generated when the banknote has been moved from the escrow position to a safe
 * position within the validator system where the banknote cannot be retrieved by the user.
 * At this point, it is safe for the host to use this event as it's 'Credit' point.
 */
class NoteCredit(denomination: Denomination) : SspEvent(SspEventCode.NoteCredit, denomination) {

    override fun stringify(short: Boolean, indent: String): String =
        if (short) {
            "Credit - ${denomination.stringify(true)}"
        } else {
            "Credited Note - ${denomination.stringify(false)}"
        }
}
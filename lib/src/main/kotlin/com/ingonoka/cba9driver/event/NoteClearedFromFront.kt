/*
 * Copyright (c) 2023. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driver.event

import com.ingonoka.cba9driver.data.Denomination

/**
 * During the device power-up sequence a bill was detected as being in the note path. This bill is
 * then rejected from the device via the bezel and this event is issued. If the bill value is
 * known then the channel number is given in the data byte, otherwise the data byte will be zero value.
 */
class NoteClearedFromFront(denomination: Denomination) : SspEvent(SspEventCode.NoteClearedFromFront, denomination) {

    override fun stringify(short: Boolean, indent: String): String =
        if (short) {
            "Cleared Front - Php $denomination"
        } else {
            "Note cleared from front - Php $denomination"
        }
}
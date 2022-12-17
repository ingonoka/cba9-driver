/*
 * Copyright (c) 2022. Ingo Noka
 * This file belongs to project load-kiosk.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driver.event

import com.ingonoka.cba9driver.data.Denomination

/**
 * During the device power-up sequence a bill was detected as being in the stack path. This bill
 * is then moved into the device cashbox and this event is issued. If the bill value is known then
 * the channel number is given in the data byte, otherwise the data byte will be zero value.
 */
class NoteClearedIntoCashbox(denomination: Denomination) :
    SspEvent(SspEventCode.NoteClearedIntoCashbox, denomination) {

    override fun stringify(short: Boolean, indent: String): String =
        if (short) {
            "Cleared cashbox - Php $denomination"
        } else {
            "Note cleared into cash box - Php $denomination"
        }
}
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
 * An META event generated by the driver by converting a Read(channel) event that has a non-zero channel.
 *
 * The BNA will create a READ(channel) event to indicate that a banknote with a particular denomination was
 * successfully scanned and is now in escrow. A POLL command after that will initiate the stacking process.
 */
class AcceptedNote(denomination: Denomination) : Read(denomination) {


    override fun stringify(short: Boolean, indent: String): String =
        if (short) {
            "Accept ${denomination.stringify(true)}"
        } else {
            "Banknote accepted for stacking in channel: ${denomination.stringify(false)}"
        }
}
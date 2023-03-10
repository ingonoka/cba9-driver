/*
 * Copyright (c) 2023. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driver.event

import com.ingonoka.cba9driver.data.Denominations

/**
 * An META event generated by the driver after a successful set inhibits command that activated at least one channel.
 * [denominations] contains the activated channels with associated denomination.
 */
class ChannelEnabled(val denominations: Denominations) : SspEvent(SspEventCode.ChannelEnabled) {

    override fun stringify(short: Boolean, indent: String): String =
        if (short) {
            "Channel Enabled: ${denominations.stringify(true)}"
        } else {
            "Channel Enabled: ${denominations.stringify(false)}"
        }
}
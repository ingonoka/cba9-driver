/*
 * Copyright (c) 2022. Ingo Noka
 * This file belongs to project load-kiosk.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driver.data

/**
 * Instead of using a vanilla integer for channel numbers, this class is used to make reading code easier.
 */
@JvmInline
value class ChannelNumber(val channelNumber: Int) {

    init {
        if(channelNumber !in 0..Byte.MAX_VALUE)
            throw RuntimeException("Channel number must be 0 to ${Byte.MAX_VALUE} inclusive.  Is: $channelNumber")
    }

    override fun toString(): String = "%02d".format(channelNumber)
}
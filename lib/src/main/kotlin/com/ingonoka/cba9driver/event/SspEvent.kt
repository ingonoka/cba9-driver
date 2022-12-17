/*
 * Copyright (c) 2022. Ingo Noka
 * This file belongs to project load-kiosk.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driver.event

import com.ingonoka.cba9driver.data.CountryCode
import com.ingonoka.cba9driver.data.Denomination
import com.ingonoka.cba9driver.util.Stringifiable
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * Comments copied from "Protocol Manual SSP", version GA138_2_2_2000A, Innovative Technology Limited.
 */

sealed class SspEvent(
    val sspEventCode: SspEventCode,
    val denomination: Denomination = Denomination(0, CountryCode.UNKNOWN),
    val time: Instant = Clock.System.now()
) : Stringifiable {
    override fun stringify(short: Boolean, indent: String): String = sspEventCode.name

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SspEvent

        if (sspEventCode != other.sspEventCode) return false
        if (denomination != other.denomination) return false
        if (time != other.time) return false

        return true
    }

    override fun hashCode(): Int {
        var result = sspEventCode.hashCode()
        result = 31 * result + denomination.hashCode()
        result = 31 * result + time.hashCode()
        return result
    }
}
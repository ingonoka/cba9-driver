/*
 * Copyright (c) 2022. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driver.data

import com.ingonoka.cba9driver.util.Stringifiable

/**
 * Class holding the value of a denomination in currency of the device configuration
 */
class Denomination(val denomination: Int, val countryCode: CountryCode) : Stringifiable {

    fun isUnknown() = denomination == 0 && countryCode == CountryCode.UNKNOWN

    override fun toString(): String = stringify(true)

    override fun stringify(short: Boolean, indent: String): String =
        if (denomination != 0) {
            if (short)
                "${countryCode.name}%d".format(denomination)
            else
                "${countryCode.longName} %.2f".format(denomination.toFloat())

        } else {
            "UNKNOWN"
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Denomination) return false

        if (denomination != other.denomination) return false
        if (countryCode != other.countryCode) return false

        return true
    }

    override fun hashCode(): Int {
        var result = denomination
        result = 31 * result + countryCode.hashCode()
        return result
    }
}

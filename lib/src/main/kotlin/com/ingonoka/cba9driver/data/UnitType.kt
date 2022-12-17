/*
 * Copyright (c) 2022. Ingo Noka
 * This file belongs to project load-kiosk.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driver.data

import com.ingonoka.cba9driver.util.ReadIntBuffer
import com.ingonoka.cba9driver.util.Stringifiable
import com.ingonoka.cba9driver.util.WriteIntBuffer

/**
 * Enumeration of unit types that can be returned in messages of the SSP protocol
 */
enum class UnitType(val unitTypeCode: Int, val shortName: String, val longName: String) : Stringifiable {
    UnknownType(-1, "", ""),
    BanknoteValidator(0, "BNV", "Banknote Validator");

    fun encode(buf: WriteIntBuffer): Result<WriteIntBuffer> = try {

        buf.writeByte(unitTypeCode)

        Result.success(buf)

    } catch (e: Exception) {

        Result.failure(e)
    }

    companion object {

        internal fun build(bytes: ReadIntBuffer): Result<UnitType> = try {
            require(bytes.hasBytesLeftToRead(1))

            val code = bytes.readByte().getOrThrow()

            unitTypeForCode(code)

        } catch (e: Exception) {

            Result.failure(e)
        }

        private fun unitTypeForCode(unitTypeCode: Int): Result<UnitType> = try {
            val unitType = values().find {
                it.unitTypeCode == unitTypeCode
            } ?: throw Exception("Failed to find unit type for code: $unitTypeCode")

            Result.success(unitType)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    override fun stringify(short: Boolean, indent: String): String =
        if (short) {
            shortName
        } else {
            "Unit Type: $longName"
        }
}
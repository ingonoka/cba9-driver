/*
 * Copyright (c) 2022. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driver.response

import com.ingonoka.cba9driver.data.CountryCode
import com.ingonoka.cba9driver.data.FirmwareVersion
import com.ingonoka.cba9driver.data.UnitType
import com.ingonoka.cba9driver.data.ValueMultiplier
import com.ingonoka.cba9driver.util.*

/**
 * Data returned by the CBA9 in response to a unit data request
 */
class UnitDataResponseData(
    val unitType: UnitType,
    val firmwareVersion: FirmwareVersion,
    val countryCode: CountryCode,
    val valueMultiplier: ValueMultiplier,
    val protocolVersion: Int
) : SspResponseData(), Stringifiable {

    fun encode(buf: WriteIntBuffer = IntBuffer.empty()): Result<WriteIntBuffer> = try {

        unitType.encode(buf).getOrThrow()
        firmwareVersion.encode(buf).getOrThrow()
        countryCode.encode(buf).getOrThrow()
        valueMultiplier.encode(buf, ByteOrder.LITTLE_ENDIAN).getOrThrow()
        buf.writeByte(protocolVersion)

        Result.success(buf)

    } catch (e: Exception) {

        Result.failure(e)
    }

    companion object {

        internal fun decode(data: List<Int>): Result<UnitDataResponseData> = decode(IntBuffer.wrap(data))

        internal fun decode(bytes: ReadIntBuffer): Result<UnitDataResponseData> = try {

            val unitType = UnitType.build(bytes).getOrThrow()

            val firmwareVersion = FirmwareVersion.build(bytes).getOrThrow()

            val countryCode = CountryCode.build(bytes).getOrThrow()

            val valueMultiplier = ValueMultiplier.build(bytes, ByteOrder.LITTLE_ENDIAN).getOrThrow()

            val protocolVersion = bytes.readByte().getOrThrow()

            Result.success(
                UnitDataResponseData(
                    unitType,
                    firmwareVersion,
                    countryCode,
                    valueMultiplier,
                    protocolVersion
                )
            )

        } catch (e: Exception) {

            Result.failure(Exception("Failed creation of setup data", e))
        }
    }

    override fun toString(): String = stringify()

    override fun stringify(short: Boolean, indent: String): String =
        if (short) {
            "${unitType.shortName} - fw:$firmwareVersion,pv:$protocolVersion,cc:$countryCode,multiplier:$valueMultiplier"
        } else {
            """
            |${unitType.longName}:
            |   Firmware Version:       $firmwareVersion
            |   Protocol Version:       $protocolVersion
            |   Country Code:           $countryCode
            |   Multiplier:             $valueMultiplier
            """.trimMargin()
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UnitDataResponseData

        if (unitType != other.unitType) return false
        if (firmwareVersion != other.firmwareVersion) return false
        if (countryCode != other.countryCode) return false
        if (valueMultiplier != other.valueMultiplier) return false
        if (protocolVersion != other.protocolVersion) return false

        return true
    }

    override fun hashCode(): Int {
        var result = unitType.hashCode()
        result = 31 * result + firmwareVersion.hashCode()
        result = 31 * result + countryCode.hashCode()
        result = 31 * result + valueMultiplier.hashCode()
        result = 31 * result + protocolVersion
        return result
    }
}
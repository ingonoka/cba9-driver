/*
 * Copyright (c) 2023. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driver.response

import com.ingonoka.cba9driver.data.*
import com.ingonoka.cba9driver.util.*

/**
 * Data returned by the CBA9 in response to a setup data request
 */
class SetupResponseData(
    val unitType: UnitType = UnitType.UnknownType,
    val firmwareVersion: FirmwareVersion = FirmwareVersion(),
    val countryCode: CountryCode = CountryCode.UNKNOWN,
    val valueMultiplier: ValueMultiplier = ValueMultiplier(-1),
    val maxChannelNumber: Int = -1,
    val bankNoteDenominations: Denominations = Denominations(mapOf()),
    val channelSecurities: List<ChannelSecurity> = listOf(),
    val realValueMultiplier: Int = -1,
    val protocolVersion: Int = -1
) : SspResponseData(), Stringifiable {

    fun encode(buf: WriteIntBuffer = IntBuffer.empty()): Result<WriteIntBuffer> = try {

        unitType.encode(buf).getOrThrow()
        firmwareVersion.encode(buf).getOrThrow()
        countryCode.encode(buf).getOrThrow()
        valueMultiplier.encode(buf, ByteOrder.BIG_ENDIAN).getOrThrow()
        buf.writeByte(maxChannelNumber)
        bankNoteDenominations.encode(buf, valueMultiplier).getOrThrow()
        channelSecurities.forEach {
            it.encode(buf).getOrThrow()
        }
        buf.write(realValueMultiplier, 3)
        if (valueMultiplier.value == 0) {
            bankNoteDenominations.encodeCountryCodes(buf).getOrThrow()
            bankNoteDenominations.encodeDenominationValues(buf).getOrThrow()
        }
        buf.writeByte(protocolVersion)

        bankNoteDenominations.denominations.forEach {
            buf.write(it.value.countryCode.name)
        }

        bankNoteDenominations.denominations.forEach {
            buf.write(it.value.denomination,4, ByteOrder.LITTLE_ENDIAN)
        }


        Result.success(buf)

    } catch (e: Exception) {

        Result.failure(e)
    }

    companion object {

        internal fun decode(data: List<Int>) : Result<SetupResponseData> = decode(IntBuffer.wrap(data))

        internal fun decode(bytes: ReadIntBuffer): Result<SetupResponseData> = try {

            val unitType = UnitType.build(bytes).getOrThrow()

            val firmwareVersion = FirmwareVersion.build(bytes).getOrThrow()

            val countryCode = CountryCode.build(bytes).getOrThrow()

            val valueMultiplier = ValueMultiplier.build(bytes, ByteOrder.BIG_ENDIAN).getOrThrow()

            val maxChannelNumber = bytes.readByte().getOrThrow()

            val bankNoteDenominations =
                Denominations.build(bytes, countryCode, maxChannelNumber, valueMultiplier).getOrThrow()

            val channelSecurities = List(maxChannelNumber) {
                ChannelSecurity.build(bytes).getOrThrow()
            }

            val realValueMultiplier = bytes.readInt(3).getOrThrow()

            val usesDenominations = if (valueMultiplier.value == 0) {

                val countryCodes = List(maxChannelNumber) {
                    CountryCode.build(bytes).getOrThrow()
                }

                val channelValues = List(maxChannelNumber) {
                    bytes.readByte().getOrThrow()
                }

                val d = (0 until maxChannelNumber).map {
                    ChannelNumber(it)
                }.zip(
                    channelValues.zip(countryCodes).map {
                        Denomination(it.first, it.second)
                    }).toMap()

                Denominations(d)

            } else {
                bankNoteDenominations
            }

            val protocolVersion = bytes.readByte().getOrThrow()

            Result.success(
                SetupResponseData(
                    unitType,
                    firmwareVersion,
                    countryCode,
                    valueMultiplier,
                    maxChannelNumber,
                    usesDenominations,
                    channelSecurities,
                    realValueMultiplier,
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
            indent + "TYPE:${unitType.shortName}," +
                    "FW:$firmwareVersion," +
                    "${countryCode}," +
                    "$valueMultiplier," +
                    "MAX_CHANNEL:$maxChannelNumber," +
                    bankNoteDenominations.denominations.toList().zip(channelSecurities).joinToString(",") {
                        "${it.first.first.channelNumber}=${it.first.second}/${it.second.name}"
                    } +
                    ",REAL_VALUE_MULTIPLIER:$realValueMultiplier," +
                    "PROTOCOL:$protocolVersion"
        } else {
            """
            |${indent}${unitType.longName}:
            |${indent}   Firmware Version:       ${firmwareVersion.stringify(false, "")}
            |${indent}   Country Code:           $countryCode
            |${indent}   Value Multiplier:       $valueMultiplier
            |${indent}   Max Channel Number:     $maxChannelNumber
            |${indent}   Denominations:          ${bankNoteDenominations.stringify(false, "")}
            |${indent}   Channel Securities:     $channelSecurities
            |${indent}   Real Value Multiplier:  $realValueMultiplier
            |${indent}   Protocol Version:       $protocolVersion
""".trimMargin()
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SetupResponseData

        if (unitType != other.unitType) return false
        if (firmwareVersion != other.firmwareVersion) return false
        if (countryCode != other.countryCode) return false
        if (valueMultiplier != other.valueMultiplier) return false
        if (maxChannelNumber != other.maxChannelNumber) return false
        if (bankNoteDenominations != other.bankNoteDenominations) return false
        if (channelSecurities != other.channelSecurities) return false
        if (realValueMultiplier != other.realValueMultiplier) return false
        if (protocolVersion != other.protocolVersion) return false

        return true
    }

    override fun hashCode(): Int {
        var result = unitType.hashCode()
        result = 31 * result + firmwareVersion.hashCode()
        result = 31 * result + countryCode.hashCode()
        result = 31 * result + valueMultiplier.hashCode()
        result = 31 * result + maxChannelNumber
        result = 31 * result + bankNoteDenominations.hashCode()
        result = 31 * result + channelSecurities.hashCode()
        result = 31 * result + realValueMultiplier
        result = 31 * result + protocolVersion
        return result
    }
}
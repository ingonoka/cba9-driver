/*
 * Copyright (c) 2022. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driver.response

import com.ingonoka.cba9driver.data.ChannelNumber
import com.ingonoka.cba9driver.data.CountryCode
import com.ingonoka.cba9driver.data.Denomination
import com.ingonoka.cba9driver.data.Denominations
import com.ingonoka.cba9driver.util.*

/**
 * Data returned by the CBA9 in response to a setup data request
 */
class ChannelValueResponseData(
    val bankNoteDenominations: Denominations,
) : SspResponseData(), Stringifiable {

    fun encode(
        buf: WriteIntBuffer = IntBuffer.empty(),
        protocolVersion: Int,
        valueMultiplier: Int = 1
    ): Result<WriteIntBuffer> = try {

        if (bankNoteDenominations.denominations.isNotEmpty()) {
            val maxChannelNumber = bankNoteDenominations.denominations.size
            buf.writeByte(maxChannelNumber)


            if (protocolVersion < 6) {
                bankNoteDenominations.denominations.values.forEach {
                    buf.writeByte(it.denomination / valueMultiplier)
                }
            } else {

                buf.write(List(maxChannelNumber) { 0 })

                bankNoteDenominations.denominations.values.forEach {
                    it.countryCode.encode(buf)
                    if (it.denomination == 0) {
                        buf.writeByte(0)
                    }
                }

                bankNoteDenominations.denominations.values.forEach {
                    buf.write(it.denomination, 4, ByteOrder.LITTLE_ENDIAN)
                }
            }
        }

        Result.success(buf)

    } catch (e: Exception) {

        Result.failure(e)
    }

    companion object {

        internal fun decode(
            bytes: List<Int>,
            protocolVersion: Int,
            fixedCountryCode: CountryCode = CountryCode.UNKNOWN,
            valueMultiplier: Int = 1
        ): Result<ChannelValueResponseData> = decode(IntBuffer.wrap(bytes), protocolVersion, fixedCountryCode, valueMultiplier)

        internal fun decode(
            buf: ReadIntBuffer,
            protocolVersion: Int,
            fixedCountryCode: CountryCode = CountryCode.UNKNOWN,
            valueMultiplier: Int = 1
        ): Result<ChannelValueResponseData> = try {

            // max channel number
            val maxChannelNumber = buf.readByte().getOrThrow()

            // values for protocol versions < 6
            val channelValuesShort = buildList {
                repeat(maxChannelNumber) {
                    add(buf.readByte().getOrThrow() * valueMultiplier)
                }
            }

            val bankNoteDenominations = if (protocolVersion < 6) {

                channelValuesShort.mapIndexed { index, value ->
                    ChannelNumber(index + 1) to Denomination(value, fixedCountryCode)
                }.toMap()

            } else {

                val channelCountryCodes = buildList {
                    repeat(maxChannelNumber) {
                        add(CountryCode.build(buf).getOrThrow())
                        // based on sample data in GA138 SSP Manual 2_2_2000A
                        // unused channels seem to have an extra 0
                        buf.peekByte().onSuccess { if (it == 0) buf.readByte() }
                    }
                }

                val channelValuesLong = buildList {
                    repeat(maxChannelNumber) {
                        add(buf.readInt(4, ByteOrder.LITTLE_ENDIAN).getOrThrow())
                    }
                }

                channelValuesLong.zip(channelCountryCodes).mapIndexed { index, pair ->
                    val (value, countryCode) = pair
                    ChannelNumber(index + 1) to Denomination(value, countryCode)
                }.toMap()

            }

            val denominations = Denominations(denominations = bankNoteDenominations)

            Result.success(ChannelValueResponseData(denominations))

        } catch (e: Exception) {

            Result.failure(Exception("Failed creation of setup data", e))
        }
    }

    override fun toString(): String = stringify()

    override fun stringify(short: Boolean, indent: String): String =
        if (short) {
            bankNoteDenominations.stringify(true)
        } else {
            """
            |   Maximum Channels:       ${bankNoteDenominations.denominations.size}
            |   Denominations:          ${bankNoteDenominations.stringify(false)}
            """.trimMargin()
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ChannelValueResponseData) return false

        if (bankNoteDenominations != other.bankNoteDenominations) return false

        return true
    }

    override fun hashCode(): Int {
        return bankNoteDenominations.hashCode()
    }
}
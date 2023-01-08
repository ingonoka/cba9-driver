/*
 * Copyright (c) 2023. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driver.data

import com.ingonoka.cba9driver.util.ReadIntBuffer
import com.ingonoka.cba9driver.util.Stringifiable
import com.ingonoka.cba9driver.util.WriteIntBuffer
import java.util.*

/**
 * Class holding a map of channel number to denomination.  The denomination is the value of the banknote
 * assigned to the channel number (in the currency of the device configuration)
 *
 * The CB9 will return the channel number in the response for some commands and this class can be used to map the number
 * to the value of the banknote
 */
class Denominations(val denominations: Map<ChannelNumber, Denomination>) : Stringifiable {

    fun channelDenomination(channel: ChannelNumber): Denomination =
        denominations[channel] ?: Denomination(0, CountryCode.UNKNOWN)

    fun denominationChannel(denomination: Denomination): Result<ChannelNumber> = try {

        val channelNumber = denominations.filterValues { it == denomination }.keys.first()

        Result.success(channelNumber)

    } catch(e: Exception) {

        Result.failure(e)
    }

    /**
     * Get a [Denominations] object that only includes the channels and denominations of [includeDenominations]
     */
    fun getSubset(includeDenominations: List<Denomination>): Denominations {
        val includedChannels = denominations.filter {
            includeDenominations.contains(it.value)
        }
        return Denominations(includedChannels)
    }

    /**
     * Convert a list of denominations to two bytes with the bit corresponding to the denomination channel
     * set to 1.
     */
    fun getInhibits(includeDenominations: List<Denomination>): List<Int> {
        var inhibits = 0
        val includedChannels = denominations.filter {
            includeDenominations.contains(it.value)
        }.map {
            it.key.channelNumber
        }

        includedChannels.forEach { pos ->
            inhibits = inhibits or (1 shl (pos - 1))
        }

        return listOf((inhibits and 0xFF), ((inhibits and 0xFF00) ushr 8))

    }

    fun encodeCountryCodes(buf: WriteIntBuffer): Result<WriteIntBuffer> = try {

        denominations.forEach {
            it.value.countryCode.encode(buf)
        }

        Result.success(buf)

    } catch (e: Exception) {

        Result.failure(e)
    }

    fun encodeDenominationValues(buf: WriteIntBuffer): Result<WriteIntBuffer> = try {

        denominations.forEach {
            buf.writeByte(it.value.denomination)
        }

        Result.success(buf)

    } catch (e: Exception) {

        Result.failure(e)
    }

    fun encode(buf: WriteIntBuffer, multiplier: ValueMultiplier): Result<WriteIntBuffer> = try {

        denominations.forEach {
            buf.writeByte(it.value.denomination / multiplier.value)
        }

        Result.success(buf)

    } catch (e: Exception) {

        Result.failure(e)
    }

    companion object {

        internal fun build(
            bytes: ReadIntBuffer,
            countryCode: CountryCode,
            numberOfChannels: Int,
            multiplier: ValueMultiplier
        ): Result<Denominations> = try {

            val denominations = buildMap {
                for (channel in 1..numberOfChannels) {
                    val denominationValue = bytes.readByte().getOrThrow()
                    put(ChannelNumber(channel), Denomination(denominationValue * multiplier.value, countryCode))
                }
            }

            Result.success(Denominations(denominations))

        } catch (e: Exception) {

            Result.failure(Exception("Failed creation of denominations map", e))
        }
    }

    override fun stringify(short: Boolean, indent: String): String =
        if (short) {
            denominations.values.joinToString(",", "[", "]") {
                it.stringify(true)
            }
        } else {
            denominations.entries.joinToString(",", "[", "]") {
                "${it.key} - ${it.value.stringify(false)}"
            }
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Denominations) return false

        if(denominations.size != other.denominations.size) return false

        if(denominations.any {
            val (channelNumber, denomination) = it
            other.denominations[channelNumber] != denomination
        }) return false

        return true
    }

    override fun hashCode(): Int {
        return denominations.hashCode()
    }
}

/**
 * Convert a string of denominations (usually from a config) to a [Denominations] object.
 *
 * The string has the form: <Currency Code><Amount>
 *
 * The currency code must be one of the supported codes defined in [CountryCode].  The value must be a decimal integer
 * number.
 *
 * Example: "PHP20,PHP50,PHP100"
 *
 */

fun String.toDenominations(): Result<List<Denomination>> = try {

    if (isEmpty()) throw Exception("String is empty")

    val denominationStrings = split(",")

    val result = mutableListOf<Denomination>()

    for (element in denominationStrings) {
        val currency = element.take(3)
        val amount = element.drop(3).toInt(10)
        result.add(Denomination(amount, CountryCode.valueOf(currency.uppercase(Locale.ENGLISH))))
    }

    Result.success(result.toList())

} catch (e: Exception) {

    Result.failure(Exception("Failed to parse string of denominations: $this", e))
}
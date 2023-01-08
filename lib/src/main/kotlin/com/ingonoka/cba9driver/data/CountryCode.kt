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
import com.ingonoka.cba9driver.util.WriteIntBuffer


/**
 * Enumeration of country codes.  It is actual a 3 character ASCII identifier of the currency of the dataset
 * configured for the device.
 */
enum class CountryCode(val countryCode: String, val longName: String) {
    UNKNOWN("   ", "Unknown Country Code"),
    EUR("EUR", "Euros"),
    PHP("PHP", "Philippine Pesos");

    fun encode(buf: WriteIntBuffer): Result<WriteIntBuffer> = try {

        buf.write(countryCode)

        Result.success(buf)

    } catch (e: Exception) {

        Result.failure(e)
    }

    companion object {

        internal fun build(bytes: ReadIntBuffer): Result<CountryCode> = try {

            val countryCode = bytes
                .readString(3)
                .mapCatching { countryCodeForCode(it).getOrThrow() }
                .getOrThrow()

            Result.success(countryCode)

        } catch (e: Exception) {

            Result.failure(Exception("Failed creation of country code", e))
        }

        private fun countryCodeForCode(code: String): Result<CountryCode> = try {
            val countryCode = values().find {
                it.countryCode == code
            } ?: throw Exception("Unknown country code: $code")

            Result.success(countryCode)

        } catch (e: Exception) {

            Result.failure(Exception("Failed to create country code", e))

        }
    }
}
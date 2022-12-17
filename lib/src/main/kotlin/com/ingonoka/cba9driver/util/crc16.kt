/*
 * Copyright (c) 2022. Ingo Noka
 * This file belongs to project load-kiosk.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driver.util

/**
 * The seed value for the SSP CRC algorithm
 */
const val crcSeedValue: Int = 0xFFFF

/**
 * The polynomial  for the SSP CRC algorithm
 */
const val polynomial: Int = 0x8005

/**
 * Pre-calculated CRC16 table for all 256 possible values of a byte.
 */
val crc16Table = (0 until 256).map { crc16(it) }

/**
 * Generate CC16 checksum over array of unsigned bytes [inputs].  Return result as unsigned short.
 */
private fun crc16(inputs: List<Int>): Int {
    return inputs.fold(crcSeedValue) { remainder, byte ->
        val bigEndianInput = byte shl 8
        val index = (bigEndianInput xor remainder) shr 8
        crc16Table[index] xor (remainder shl 8 and 0xFFFF)
    }
}

/**
 * Generate CRC16 checksum of one byte [input] with [polynomial]. Return result as unsigned short.
 */
private fun crc16(input: Int): Int {

    val bigEndianInput = (input and 0xFF) shl 8
    return (0 until 8).fold(bigEndianInput) { result, _ ->
        val isMostSignificantBitOne = (result and 0x8000) != 0
        val shiftedResult = (result shl 1 and 0xffff)
        when (isMostSignificantBitOne) {
            true -> shiftedResult xor polynomial
            false -> shiftedResult
        }
    }
}

/**
 * Generate CRC16 checksum over byte array [inputs]. Return [size] bytes of the result.
 */
fun crc16(inputs: List<Int>, size: Int = 2): List<Int> {
    val crc = crc16(inputs.map { it and 0xFF })
    return when (size) {
        4 -> listOf(
            ((crc) and 0xFF).toByte().toInt(),
            ((crc shr 8) and 0xFF).toByte().toInt(),
            ((crc shr 16) and 0xFF).toByte().toInt(),
            ((crc shr 24) and 0xFF).toByte().toInt()
        )
        2 -> listOf(
            ((crc) and 0xFF).toByte().toInt(),
            ((crc shr 8) and 0xFF).toByte().toInt()
        )
        else -> throw IllegalArgumentException()
    }
}

/**
 * Verify whether the crc is correct. Calculate the crc16 of [buf] and return true if it is the same as [crc].
 * [buf] only contains the data over which to calculate the crc16.
 *
 * The length of the crc in number of bytes is the length of [crc].
 */
fun crc16Validate(buf: List<Int>, crc: List<Int>): Boolean {
    val crcCalculated = crc16(buf, crc.size)
    return crcCalculated == crc
}
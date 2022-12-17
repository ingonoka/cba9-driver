/*
 * Copyright (c) 2022. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

@file:Suppress("DuplicatedCode")

package com.ingonoka.cba9driver.util

import com.ingonoka.cba9driver.util.ByteOrder.*
import kotlin.math.min

/**
 * Use [numBytes] bytes of the [ByteArray] and interpret as  [Int] without leading zeros. Start reading at [index]
 *
 * An empty array yields 0.
 *
 * For example `0x0080` and `0x80` are both converted to 128 and NOT to 128 and -127 respectively
 *
 * If [numBytes] is 4 than the resulting [Int] can be negative if the left-most bit is set to 1.
 * For example `0xFFFFFFFF` will be -1.
 *
 * if [numBytes] is 0 than all remaining bytes of the byte array are used (max 4)
 *
 */

fun ByteArray.toIntNoLeadingZeros(
    numBytes: Int = 0,
    index: Int = 0,
    endian: ByteOrder = BIG_ENDIAN
): Result<Int> = try {

    if (numBytes !in 0..4) throw Exception("numBytes must be between 0 and 4. Is: $numBytes")

    if (isEmpty()) throw Exception("ByteArray Empty.  Cannot read Int")

    if (index !in indices) throw Exception("index must be between 0 and $size. Is: $index")

    val length = if (numBytes == 0) min(4, size - index) else numBytes

    if (index + numBytes > size) throw Exception("Not enough bytes in array for index $index and numBytes ${length}. Need ${numBytes}, have ${size - index}")

    val i = when (length) {

        0 -> 0

        1 -> (get(index).toInt()) and 0xFF

        2 -> if (endian == BIG_ENDIAN) {
            ((get(index).toInt() and 0xFF) shl 8) or
                    (get(index + 1).toInt() and 0xFF)
        } else {
            (get(index + 1).toInt() and 0xFF) shl 8 or
                    (get(index).toInt() and 0xFF)
        }

        3 -> if (endian == BIG_ENDIAN) {
            (((get(index).toInt() and 0xFF)) shl 16) or
                    (((get(index + 1).toInt() and 0xFF)) shl 8) or
                    (get(index + 2).toInt() and 0xFF)
        } else {
            ((get(index + 2).toInt() and 0xFF) shl 16) or
                    ((get(index + 1).toInt() and 0xFF) shl 8) or
                    (get(index).toInt() and 0xFF)
        }

        else -> if (endian == BIG_ENDIAN) {
            ((get(index).toInt() and 0xFF) shl 24) or
                    ((get(index + 1).toInt() and 0xFF) shl 16) or
                    ((get(index + 2).toInt() and 0xFF) shl 8) or
                    (get(index + 3).toInt() and 0xFF)
        } else {
            ((get(index + 3).toInt() and 0xFF) shl 24) or
                    ((get(index + 2).toInt() and 0xFF) shl 16) or
                    ((get(index + 1).toInt() and 0xFF) shl 8) or
                    (get(index).toInt() and 0xFF)
        }
    }

    Result.success(i)

} catch (e: Exception) {

    Result.failure(e)
}

/**
 * @see [toIntNoLeadingZeros]
 *
 * The integer values in the list must each be in 0..0xFF
 */
fun List<Int>.toIntNoLeadingZeros(
    numBytes: Int = 0,
    index: Int = 0,
    endian: ByteOrder = BIG_ENDIAN
): Result<Int> = try {

    if (numBytes !in 0..4) throw Exception("numBytes must be between 0 and 4. Is: $numBytes")

    if (isEmpty()) throw Exception("ByteArray Empty.  Cannot read Int")

    if (index !in indices) throw Exception("index must be between 0 and $size. Is: $index")

    val length = if (numBytes == 0) min(4, size - index) else numBytes

    if (index + numBytes > size) throw Exception("Not enough bytes in array for index $index and numBytes ${length}. Need ${numBytes}, have ${size - index}")

    val i = when (length) {

        0 -> 0
        1 -> get(index) and 0xFF
        2 -> if (endian == BIG_ENDIAN) {
            ((get(index) and 0xFF) shl 8) or
                    (get(index + 1) and 0xFF)
        } else {
            ((get(index + 1) and 0xFF) shl 8) or
                    (get(index) and 0xFF)
        }

        3 -> if (endian == BIG_ENDIAN) {
            ((get(index) and 0xFF) shl 16) or
                    ((get(index + 1) and 0xFF) shl 8) or
                    (get(index + 2) and 0xFF)
        } else {
            ((get(index + 2) and 0xFF) shl 16) or
                    ((get(index + 1) and 0xFF) shl 8) or
                    (get(index) and 0xFF)
        }

        else -> if (endian == BIG_ENDIAN) {
            ((get(index) and 0xFF) shl 24) or
                    ((get(index + 1) and 0xFF) shl 16) or
                    ((get(index + 2) and 0xFF) shl 8) or
                    ((get(index + 3) and 0xFF))
        } else {
            (get(index) and 0xFF) or
                    ((get(index + 1) and 0xFF) shl 8) or
                    ((get(index + 2) and 0xFF) shl 16) or
                    ((get(index + 3) and 0xFF) shl 24)
        }
    }

    Result.success(i)

} catch (e: Exception) {

    Result.failure(e)
}

/**
 * Use four bytes of the [ByteArray] and interpret as  [Int] . Start reading at [index]
 *
 * An empty array yields 0.
 */
fun ByteArray.toInt(index: Int = 0): Result<Int> =
    when {
        index + 4 > size -> Result.failure(Exception("Only ${size - index} bytes left in array.  Need 4 for an Int"))
        else -> Result.success(
            (((get(index).toInt() and 0xFF)) shl 24) or
                    (((get(index + 1).toInt() and 0xFF)) shl 16) or
                    (((get(index + 2).toInt() and 0xFF)) shl 8) or
                    (get(index + 3).toInt() and 0xFF)
        )
    }


/**
 * Use [numBytes] bytes of the [ByteArray] and interpret as  [Long] without leading zeros. Start reading at [index]
 *
 * * A failure result will be returned if:
 *
 * - [numBytes] is larger than 8
 * - [numBytes] is zero
 * - [numBytes] is larger than the remaining bytes in the packet
 *
 * If [numBytes] is not provided it will be set to the number of remaining bytes in the packet
 * An empty array yields 0L.
 *
 * For example `0x0080` and `0x80` are both converted to 128 and NOT to 128 and -127 respectively
 *
 * If [numBytes] is 8 than the resulting [Long] can be negative if the left-most bit is set to 1.
 * For example `0xFFFFFFFFFFFFFFFF` will be -1.
 *
 * if [numBytes] is 0 than all remaining bytes are used (max 8)
 *
 */
fun ByteArray.toLongNoLeadingZeros(numBytes: Int = 0, index: Int = 0): Result<Long> = try {

    if (numBytes !in 0..8) throw Exception("numBytes must be between 0 and 8. Is: $numBytes")

    if (isEmpty()) throw Exception("ByteArray Empty. Cannot read Int")

    if (index !in indices) throw Exception("index must be between 0 and $size. Is: $index")

    val length = if (numBytes == 0) min(8, size - index) else numBytes

    if (index + numBytes > size) throw Exception("Not enough bytes in array for index $index and numBytes ${length}. Need ${numBytes}, have ${size - index}")

    val l = when (length) {

        0 -> 0L

        1 -> get(index).toLong() and 0xFF

        2 -> (((get(index).toLong() and 0xFF)) shl 8) or
                (get(index + 1).toLong() and 0xFF)

        3 -> (((get(index).toLong() and 0xFF)) shl 16) or
                (((get(index + 1).toLong() and 0xFF)) shl 8) or
                (get(index + 2).toLong() and 0xFF)

        4 -> (((get(index).toLong() and 0xFF)) shl 24) or
                (((get(index + 1).toLong() and 0xFF)) shl 16) or
                (((get(index + 2).toLong() and 0xFF)) shl 8) or
                (get(index + 3).toLong() and 0xFF)

        5 -> (((get(index).toLong() and 0xFF)) shl 32) or
                (((get(index + 1).toLong() and 0xFF)) shl 24) or
                (((get(index + 2).toLong() and 0xFF)) shl 16) or
                (((get(index + 3).toLong() and 0xFF)) shl 8) or
                (get(index + 4).toLong() and 0xFF)

        6 -> (((get(index).toLong() and 0xFF)) shl 40) or
                (((get(index + 1).toLong() and 0xFF)) shl 32) or
                (((get(index + 2).toLong() and 0xFF)) shl 24) or
                (((get(index + 3).toLong() and 0xFF)) shl 16) or
                (((get(index + 4).toLong() and 0xFF)) shl 8) or
                (get(index + 5).toLong() and 0xFF)

        7 -> (((get(index).toLong() and 0xFF)) shl 48) or
                (((get(index + 1).toLong() and 0xFF)) shl 40) or
                (((get(index + 2).toLong() and 0xFF)) shl 32) or
                (((get(index + 3).toLong() and 0xFF)) shl 24) or
                (((get(index + 4).toLong() and 0xFF)) shl 16) or
                (((get(index + 5).toLong() and 0xFF)) shl 8) or
                (get(index + 6).toLong() and 0xFF)

        else -> (((get(index).toLong() and 0xFF)) shl 56) or
                (((get(index + 1).toLong() and 0xFF)) shl 48) or
                (((get(index + 2).toLong() and 0xFF)) shl 40) or
                (((get(index + 3).toLong() and 0xFF)) shl 32) or
                (((get(index + 4).toLong() and 0xFF)) shl 24) or
                (((get(index + 5).toLong() and 0xFF)) shl 16) or
                (((get(index + 6).toLong() and 0xFF)) shl 8) or
                (get(index + 7).toLong() and 0xFF)

    }

    Result.success(l)

} catch (e: Exception) {

    Result.failure(e)
}

/**
 * @see [toLongNoLeadingZeros]
 *
 * The integer values in the list must each be in 0..0xFF
 */
fun List<Int>.toLongNoLeadingZeros(numBytes: Int = 0, index: Int = 0): Result<Long> = try {

    if (numBytes !in 0..8) throw Exception("numBytes must be between 0 and 8. Is: $numBytes")

    if (isEmpty()) throw Exception("ByteArray Empty. Cannot read Int")

    if (index !in indices) throw Exception("index must be between 0 and $size. Is: $index")

    val length = if (numBytes == 0) min(8, size - index) else numBytes

    if (index + numBytes > size) throw Exception("Not enough bytes in array for index $index and numBytes ${length}. Need ${numBytes}, have ${size - index}")

    val l = when (length) {

        0 -> 0L
        1 -> get(index).toLong() and 0xFF

        2 -> (((get(index).toLong() and 0xFF)) shl 8) or
                (get(index + 1).toLong() and 0xFF)

        3 -> (((get(index).toLong() and 0xFF)) shl 16) or
                (((get(index + 1).toLong() and 0xFF)) shl 8) or
                (get(index + 2).toLong() and 0xFF)

        4 -> (((get(index).toLong() and 0xFF)) shl 24) or
                (((get(index + 1).toLong() and 0xFF)) shl 16) or
                (((get(index + 2).toLong() and 0xFF)) shl 8) or
                (get(index + 3).toLong() and 0xFF)

        5 -> (((get(index).toLong() and 0xFF)) shl 32) or
                (((get(index + 1).toLong() and 0xFF)) shl 24) or
                (((get(index + 2).toLong() and 0xFF)) shl 16) or
                (((get(index + 3).toLong() and 0xFF)) shl 8) or
                (get(index + 4).toLong() and 0xFF)

        6 -> (((get(index).toLong() and 0xFF)) shl 40) or
                (((get(index + 1).toLong() and 0xFF)) shl 32) or
                (((get(index + 2).toLong() and 0xFF)) shl 24) or
                (((get(index + 3).toLong() and 0xFF)) shl 16) or
                (((get(index + 4).toLong() and 0xFF)) shl 8) or
                (get(index + 5).toLong() and 0xFF)

        7 -> (((get(index).toLong() and 0xFF)) shl 48) or
                (((get(index + 1).toLong() and 0xFF)) shl 40) or
                (((get(index + 2).toLong() and 0xFF)) shl 32) or
                (((get(index + 3).toLong() and 0xFF)) shl 24) or
                (((get(index + 4).toLong() and 0xFF)) shl 16) or
                (((get(index + 5).toLong() and 0xFF)) shl 8) or
                (get(index + 6).toLong() and 0xFF)

        else -> (((get(index).toLong() and 0xFF)) shl 56) or
                (((get(index + 1).toLong() and 0xFF)) shl 48) or
                (((get(index + 2).toLong() and 0xFF)) shl 40) or
                (((get(index + 3).toLong() and 0xFF)) shl 32) or
                (((get(index + 4).toLong() and 0xFF)) shl 24) or
                (((get(index + 5).toLong() and 0xFF)) shl 16) or
                (((get(index + 6).toLong() and 0xFF)) shl 8) or
                (get(index + 7).toLong() and 0xFF)

    }

    Result.success(l)

} catch (e: Exception) {

    Result.failure(e)
}

/**
 * Use four bytes of the [ByteArray] and interpret as  [Long] . Start reading at [index]
 *
 * An empty array yields 0.
 */
fun ByteArray.toLong(index: Int = 0): Result<Long> =
    when {
        index + 8 > size -> Result.failure(Exception("Only ${size - index} bytes left in array.  Need 8 for a Long"))
        else -> Result.success(
            (((get(index).toLong() and 0xFF)) shl 56) or
                    (((get(index + 1).toLong() and 0xFF)) shl 48) or
                    (((get(index + 2).toLong() and 0xFF)) shl 40) or
                    (((get(index + 3).toLong() and 0xFF)) shl 32) or
                    (((get(index + 4).toLong() and 0xFF)) shl 24) or
                    (((get(index + 5).toLong() and 0xFF)) shl 16) or
                    (((get(index + 6).toLong() and 0xFF)) shl 8) or
                    (get(index + 7).toLong() and 0xFF)
        )
    }

/**
 * Convert a [ULong] to a [UByteArray]. The resulting array only has as many bytes as are necessary to store all bits
 * without any leading zero bytes.
 */
@Suppress("DuplicatedCode")
@ExperimentalUnsignedTypes
fun ULong.toByteArrayWithoutLeadingZeros(): Result<UByteArray> = Result.success(
    when {

        this < 256u -> ubyteArrayOf(
            (this and 255u).toUByte()
        )

        this < 65536u -> ubyteArrayOf(
            ((this shr 8) and 255u).toUByte(),
            (this and 255u).toUByte()

        )

        this < 16777216u -> ubyteArrayOf(
            ((this shr 16) and 255u).toUByte(),
            ((this shr 8) and 255u).toUByte(),
            (this and 255u).toUByte()
        )

        this < 4294967296u -> ubyteArrayOf(
            ((this shr 24) and 255u).toUByte(),
            ((this shr 16) and 255u).toUByte(),
            ((this shr 8) and 255u).toUByte(),
            (this and 255u).toUByte()
        )

        this < 1099511627776u -> ubyteArrayOf(
            ((this shr 32) and 255u).toUByte(),
            ((this shr 24) and 255u).toUByte(),
            ((this shr 16) and 255u).toUByte(),
            ((this shr 8) and 255u).toUByte(),
            (this and 255u).toUByte()
        )

        this < 281474976710656u -> ubyteArrayOf(
            ((this shr 40) and 255u).toUByte(),
            ((this shr 32) and 255u).toUByte(),
            ((this shr 24) and 255u).toUByte(),
            ((this shr 16) and 255u).toUByte(),
            ((this shr 8) and 255u).toUByte(),
            (this and 255u).toUByte()
        )

        this < 72057594037927936u -> ubyteArrayOf(
            ((this shr 48) and 255u).toUByte(),
            ((this shr 40) and 255u).toUByte(),
            ((this shr 32) and 255u).toUByte(),
            ((this shr 24) and 255u).toUByte(),
            ((this shr 16) and 255u).toUByte(),
            ((this shr 8) and 255u).toUByte(),
            (this and 255u).toUByte()
        )

        else -> ubyteArrayOf(
            ((this shr 56) and 255u).toUByte(),
            ((this shr 48) and 255u).toUByte(),
            ((this shr 40) and 255u).toUByte(),
            ((this shr 32) and 255u).toUByte(),
            ((this shr 24) and 255u).toUByte(),
            ((this shr 16) and 255u).toUByte(),
            ((this shr 8) and 255u).toUByte(),
            (this and 255u).toUByte()
        )
    }
)

/**
 * Convert a [Long] to a [ByteArray]. The resulting array only has as many bytes as are necessary to store all bits
 * without any leading zero bytes.  Negative [Long] input will always result in an array with 8 bytes.
 *
 * @see ULong.toByteArrayWithoutLeadingZeros
 */
@Suppress("DuplicatedCode")
fun Long.toByteArrayWithoutLeadingZeros(): Result<ByteArray> = Result.success(
    when {

        this < 0 -> byteArrayOf(
            ((this shr 56) and 255).toByte(),
            ((this shr 48) and 255).toByte(),
            ((this shr 40) and 255).toByte(),
            ((this shr 32) and 255).toByte(),
            ((this shr 24) and 255).toByte(),
            ((this shr 16) and 255).toByte(),
            ((this shr 8) and 255).toByte(),
            (this and 255).toByte()
        )

        this < 256 -> byteArrayOf(
            (this and 255).toByte()
        )

        this < 65536 -> byteArrayOf(
            ((this shr 8) and 255).toByte(),
            (this and 255).toByte()

        )

        this < 16777216 -> byteArrayOf(
            ((this shr 16) and 255).toByte(),
            ((this shr 8) and 255).toByte(),
            (this and 255).toByte()
        )

        this < 4294967296 -> byteArrayOf(
            ((this shr 24) and 255).toByte(),
            ((this shr 16) and 255).toByte(),
            ((this shr 8) and 255).toByte(),
            (this and 255).toByte()
        )

        this < 1099511627776 -> byteArrayOf(
            ((this shr 32) and 255).toByte(),
            ((this shr 24) and 255).toByte(),
            ((this shr 16) and 255).toByte(),
            ((this shr 8) and 255).toByte(),
            (this and 255).toByte()
        )

        this < 281474976710656 -> byteArrayOf(
            ((this shr 40) and 255).toByte(),
            ((this shr 32) and 255).toByte(),
            ((this shr 24) and 255).toByte(),
            ((this shr 16) and 255).toByte(),
            ((this shr 8) and 255).toByte(),
            (this and 255).toByte()
        )

        this < 72057594037927936 -> byteArrayOf(
            ((this shr 48) and 255).toByte(),
            ((this shr 40) and 255).toByte(),
            ((this shr 32) and 255).toByte(),
            ((this shr 24) and 255).toByte(),
            ((this shr 16) and 255).toByte(),
            ((this shr 8) and 255).toByte(),
            (this and 255).toByte()
        )

        else -> byteArrayOf(
            ((this shr 56) and 255).toByte(),
            ((this shr 48) and 255).toByte(),
            ((this shr 40) and 255).toByte(),
            ((this shr 32) and 255).toByte(),
            ((this shr 24) and 255).toByte(),
            ((this shr 16) and 255).toByte(),
            ((this shr 8) and 255).toByte(),
            (this and 255).toByte()
        )
    }
)
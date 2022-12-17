package com.ingonoka.cba9driver.util

/**
 * Convert a [ByteArray] to a list of Int
 */
fun ByteArray.toListOfInt(): List<Int> = map { it.toInt() }

/**
 * Convert each element of the list to a byte and convert the resulting list of bytes to a byte array.
 */
fun List<Int>.toByteArray(): ByteArray = map { it.toByte() }.toByteArray()
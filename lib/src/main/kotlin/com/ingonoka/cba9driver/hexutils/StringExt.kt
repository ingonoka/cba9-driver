/*
 * Copyright (c) 2022. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driver.hexutils

/**
 * Check whether every character in String is a hex character
 */
fun String.isHex(): Boolean = this.all { it.isHex() }

/**
 * Create a [ByteArray] from a string of Hex characters
 *
 * The following characters and character sequences will be removed before the string is converted:
 * space,comma, square brackets and "0x"
 * ```
 * "010203".hexToBytes().contentEquals(byteArrayOf(1,2,3)
 *```
 * **Removing spaces is done with regular expressions and is almost 30% slower and should be avoided**
 *
 * @return The [ByteArray]
 *
 * @Throws NumberFormatException if any character is not a hex character or the number of hex characters is less than 2 or the number of characters is not even
 */
fun String.hexToBytes(removeSpace: Boolean = false): ByteArray {

    val hexNoSpace = if (removeSpace) replace("\\s|0x|0X|[,\\[\\]]".toRegex(), "") else this

    val res = ByteArray(hexNoSpace.length / 2)

    require(hexNoSpace.length % 2 == 0) { "Only conversion of strings with even length supported: $this" }

//    hexNoSpace.chunked(2).forEachIndexed { i, chunk -> res[i] = hexMap[chunk]!! }

    var i = 0
    while (i * 2 < hexNoSpace.length) {
        res[i] = hexMap[hexNoSpace.substring(i * 2, (i * 2) + 2)]!!
        i++
    }
    return res
}

/**
 * Create a list of integers from a string of Hex characters
 *
 * @see hexToBytes
 */
fun String.hexToListOfInt(removeSpace: Boolean = false): List<Int> = hexToBytes(removeSpace).map { it.toInt() }

/**
 * Create a [Byte] from a string of one or two Hex characters
 *
 *
 */
fun String.hexToByte(): Byte = hexMap[this]!!

val hexMap = hashMapOf<String, Byte>(
    "0" to 0, "1" to 1, "2" to 2, "3" to 3, "4" to 4, "5" to 5, "6" to 6, "7" to 7, "8" to 8, "9" to 9,
    "A" to 10, "B" to 11, "C" to 12, "D" to 13, "E" to 14, "F" to 15,
    "a" to 10, "b" to 11, "c" to 12, "d" to 13, "e" to 14, "f" to 15,

    "00" to 0, "01" to 1, "02" to 2, "03" to 3, "04" to 4, "05" to 5, "06" to 6, "07" to 7, "08" to 8, "09" to 9,
    "0A" to 10, "0B" to 11, "0C" to 12, "0D" to 13, "0E" to 14, "0F" to 15,
    "0a" to 10, "0b" to 11, "0c" to 12, "0d" to 13, "0e" to 14, "0f" to 15,

    "10" to 16, "11" to 17, "12" to 18, "13" to 19, "14" to 20, "15" to 21, "16" to 22, "17" to 23, "18" to 24, "19" to 25,
    "1A" to 26, "1B" to 27, "1C" to 28, "1D" to 29, "1E" to 30, "1F" to 31,
    "1a" to 26, "1b" to 27, "1c" to 28, "1d" to 29, "1e" to 30, "1f" to 31,

    "20" to 32, "21" to 33, "22" to 34, "23" to 35, "24" to 36, "25" to 37, "26" to 38, "27" to 39, "28" to 40, "29" to 41,
    "2A" to 42, "2B" to 43, "2C" to 44, "2D" to 45, "2E" to 46, "2F" to 47,
    "2a" to 42, "2b" to 43, "2c" to 44, "2d" to 45, "2e" to 46, "2f" to 47,

    "30" to 48, "31" to 49, "32" to 50, "33" to 51, "34" to 52, "35" to 53, "36" to 54, "37" to 55, "38" to 56, "39" to 57,
    "3A" to 58, "3B" to 59, "3C" to 60, "3D" to 61, "3E" to 62, "3F" to 63,
    "3a" to 58, "3b" to 59, "3c" to 60, "3d" to 61, "3e" to 62, "3f" to 63,

    "40" to 64, "41" to 65, "42" to 66, "43" to 67, "44" to 68, "45" to 69, "46" to 70, "47" to 71, "48" to 72, "49" to 73,
    "4A" to 74, "4B" to 75, "4C" to 76, "4D" to 77, "4E" to 78, "4F" to 79,
    "4a" to 74, "4b" to 75, "4c" to 76, "4d" to 77, "4e" to 78, "4f" to 79,

    "50" to 80, "51" to 81, "52" to 82, "53" to 83, "54" to 84, "55" to 85, "56" to 86, "57" to 87, "58" to 88, "59" to 89,
    "5A" to 90, "5B" to 91, "5C" to 92, "5D" to 93, "5E" to 94, "5F" to 95,
    "5a" to 90, "5b" to 91, "5c" to 92, "5d" to 93, "5e" to 94, "5f" to 95,

    "60" to 96, "61" to 97, "62" to 98, "63" to 99, "64" to 100, "65" to 101, "66" to 102, "67" to 103, "68" to 104, "69" to 105,
    "6A" to 106, "6B" to 107, "6C" to 108, "6D" to 109, "6E" to 110, "6F" to 111,
    "6a" to 106, "6b" to 107, "6c" to 108, "6d" to 109, "6e" to 110, "6f" to 111,

    "70" to 112, "71" to 113, "72" to 114, "73" to 115, "74" to 116, "75" to 117, "76" to 118, "77" to 119, "78" to 120, "79" to 121,
    "7A" to 122, "7B" to 123, "7C" to 124, "7D" to 125, "7E" to 126, "7F" to 127,
    "7a" to 122, "7b" to 123, "7c" to 124, "7d" to 125, "7e" to 126, "7f" to 127,

    "80" to -128, "81" to -127, "82" to -126, "83" to -125, "84" to -124, "85" to -123, "86" to -122, "87" to -121, "88" to -120, "89" to -119,
    "8A" to -118, "8B" to -117, "8C" to -116, "8D" to -115, "8E" to -114, "8F" to -113,
    "8a" to -118, "8b" to -117, "8c" to -116, "8d" to -115, "8e" to -114, "8f" to -113,

    "90" to -112, "91" to -111, "92" to -110, "93" to -109, "94" to -108, "95" to -107, "96" to -106, "97" to -105, "98" to -104, "99" to -103,
    "9A" to -102, "9B" to -101, "9C" to -100, "9D" to -99, "9E" to -98, "9F" to -97,
    "9a" to -102, "9b" to -101, "9c" to -100, "9d" to -99, "9e" to -98, "9f" to -97,

    "A0" to -96, "A1" to -95, "A2" to -94, "A3" to -93, "A4" to -92, "A5" to -91, "A6" to -90, "A7" to -89, "A8" to -88, "A9" to -87,
    "a0" to -96, "a1" to -95, "a2" to -94, "a3" to -93, "a4" to -92, "a5" to -91, "a6" to -90, "a7" to -89, "a8" to -88, "a9" to -87,
    "AA" to -86, "AB" to -85, "AC" to -84, "AD" to -83, "AE" to -82, "AF" to -81,
    "aa" to -86, "ab" to -85, "ac" to -84, "ad" to -83, "ae" to -82, "af" to -81,
    "Aa" to -86, "Ab" to -85, "Ac" to -84, "Ad" to -83, "Ae" to -82, "Af" to -81,
    "aA" to -86, "aB" to -85, "aC" to -84, "aD" to -83, "aE" to -82, "aF" to -81,

    "B0" to -80, "B1" to -79, "B2" to -78, "B3" to -77, "B4" to -76, "B5" to -75, "B6" to -74, "B7" to -73, "B8" to -72, "B9" to -71,
    "b0" to -80, "b1" to -79, "b2" to -78, "b3" to -77, "b4" to -76, "b5" to -75, "b6" to -74, "b7" to -73, "b8" to -72, "b9" to -71,
    "BA" to -70, "BB" to -69, "BC" to -68, "BD" to -67, "BE" to -66, "BF" to -65,
    "ba" to -70, "bb" to -69, "bc" to -68, "bd" to -67, "be" to -66, "bf" to -65,
    "Ba" to -70, "Bb" to -69, "Bc" to -68, "Bd" to -67, "Be" to -66, "Bf" to -65,
    "bA" to -70, "bB" to -69, "bC" to -68, "bD" to -67, "bE" to -66, "bF" to -65,

    "C0" to -64, "C1" to -63, "C2" to -62, "C3" to -61, "C4" to -60, "C5" to -59, "C6" to -58, "C7" to -57, "C8" to -56, "C9" to -55,
    "c0" to -64, "c1" to -63, "c2" to -62, "c3" to -61, "c4" to -60, "c5" to -59, "c6" to -58, "c7" to -57, "c8" to -56, "c9" to -55,
    "CA" to -54, "CB" to -53, "CC" to -52, "CD" to -51, "CE" to -50, "CF" to -49,
    "ca" to -54, "cb" to -53, "cc" to -52, "cd" to -51, "ce" to -50, "cf" to -49,
    "Ca" to -54, "Cb" to -53, "Cc" to -52, "Cd" to -51, "Ce" to -50, "Cf" to -49,
    "cA" to -54, "cB" to -53, "cC" to -52, "cD" to -51, "cE" to -50, "cF" to -49,

    "D0" to -48, "D1" to -47, "D2" to -46, "D3" to -45, "D4" to -44, "D5" to -43, "D6" to -42, "D7" to -41, "D8" to -40, "D9" to -39,
    "d0" to -48, "d1" to -47, "d2" to -46, "d3" to -45, "d4" to -44, "d5" to -43, "d6" to -42, "d7" to -41, "d8" to -40, "d9" to -39,
    "DA" to -38, "DB" to -37, "DC" to -36, "DD" to -35, "DE" to -34, "DF" to -33,
    "da" to -38, "db" to -37, "dc" to -36, "dd" to -35, "de" to -34, "df" to -33,
    "Da" to -38, "Db" to -37, "Dc" to -36, "Dd" to -35, "De" to -34, "Df" to -33,
    "dA" to -38, "dB" to -37, "dC" to -36, "dD" to -35, "dE" to -34, "dF" to -33,

    "E0" to -32, "E1" to -31, "E2" to -30, "E3" to -29, "E4" to -28, "E5" to -27, "E6" to -26, "E7" to -25, "E8" to -24, "E9" to -23,
    "e0" to -32, "e1" to -31, "e2" to -30, "e3" to -29, "e4" to -28, "e5" to -27, "e6" to -26, "e7" to -25, "e8" to -24, "e9" to -23,
    "EA" to -22, "EB" to -21, "EC" to -20, "ED" to -19, "EE" to -18, "EF" to -17,
    "ea" to -22, "eb" to -21, "ec" to -20, "ed" to -19, "ee" to -18, "ef" to -17,
    "Ea" to -22, "Eb" to -21, "Ec" to -20, "Ed" to -19, "Ee" to -18, "Ef" to -17,
    "eA" to -22, "eB" to -21, "eC" to -20, "eD" to -19, "eE" to -18, "eF" to -17,

    "F0" to -16, "F1" to -15, "F2" to -14, "F3" to -13, "F4" to -12, "F5" to -11, "F6" to -10, "F7" to -9, "F8" to -8, "F9" to -7,
    "f0" to -16, "f1" to -15, "f2" to -14, "f3" to -13, "f4" to -12, "f5" to -11, "f6" to -10, "f7" to -9, "f8" to -8, "f9" to -7,
    "fa" to -6, "fb" to -5, "fc" to -4, "fd" to -3, "fe" to -2, "ff" to -1,
    "FA" to -6, "FB" to -5, "FC" to -4, "FD" to -3, "FE" to -2, "FF" to -1,
    "Fa" to -6, "Fb" to -5, "Fc" to -4, "Fd" to -3, "Fe" to -2, "Ff" to -1,
    "fA" to -6, "fB" to -5, "fC" to -4, "fD" to -3, "fE" to -2, "fF" to -1
)

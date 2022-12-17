/*
 * Copyright (c) 2022. Ingo Noka
 * This file belongs to project load-kiosk.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driver.command

/**
 * A command to allow the host to change the fixed part of the eSSP key. The eight data bytes are a 64-bit number
 * representing the fixed part of the key. This command must be encrypted.
 */
class SspSetEncryptionKey(key: List<Int>) : SspCommand(SspCommandCode.SspSetEncryptionKey, key)
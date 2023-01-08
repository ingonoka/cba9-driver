/*
 * Copyright (c) 2023. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driver.command

/**
 * Resets the fixed encryption key to the device default. The device may have extra security requirements before it
 * will accept this command (e.g. The Hopper must be empty) if these requirements are not met, the device will reply
 * with Command Cannot be Processed. If successful, the device will reply OK, then reset. When it starts up the fixed
 * key will be the default.
 *
 * This apparently only sets the fixed part back to the default: "0x01 0x23 0x45 0x67 0x01 0x23 0x45 0x67"
 */
class SspEncryptionResetToDefault : SspCommand(SspCommandCode.SspEncryptionResetToDefault)
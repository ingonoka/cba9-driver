/*
 * Copyright (c) 2022. Ingo Noka
 * This file belongs to project load-kiosk.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driver.event

import com.ingonoka.cba9driver.data.Denomination

/**
 * An event given when the BNV is reading a banknote.
 *
 * If the event data byte is zero, then the note is in the process of being scanned and validated.
 * If the data byte value changes from zero to a value greater than zero, this indicates a valid
 * banknote is now held in the escrow position. The byte value shows the channel of the banknote
 * that has been validated. A poll command after this value has been given will cause the banknote
 * to be accepted from the escrow position. The host can also issue a reject command at this point
 * to reject the banknote back to the user. The Hold command may be used to keep the banknote in
 * this position.
 */
sealed class Read(denomination: Denomination) : SspEvent(SspEventCode.Read, denomination)
/*
 * Copyright (c) 2022. Ingo Noka
 * This file belongs to project cba9-driver.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.cba9driver.event

/**
 * This event is given only when using the Poll with ACK command (though it doesn't need an event
 * ACK to be cleared as other Poll with Ack commands). It is given when the BNV is powered up and
 * setting its sensors and mechanisms to be ready for Note acceptance. When the event response does
 * not contain this event, the BNV is ready to be enabled and used.
 */
class Initialising : SspEvent(SspEventCode.Initialising)
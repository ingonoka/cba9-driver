package com.ingonoka.cba9driver

import android.hardware.usb.UsbDevice
import com.ingonoka.cba9driver.data.CountryCode
import com.ingonoka.cba9driver.data.Denomination
import com.ingonoka.usbmanager.UsbDriverProperties
import com.ingonoka.usbmanager.UsbTransferMode
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

data class Cba9Properties(
    /**
     * Property name for CBA9 protocol version.
     */
    val protocolVersion: Int = 7,
    /**
     * Denominations accepted by the BNA
     */
    val acceptedDenominations: List<Denomination> = listOf(
        Denomination(20, CountryCode.PHP),
        Denomination(50, CountryCode.PHP),
        Denomination(100, CountryCode.PHP),
        Denomination(200, CountryCode.PHP),
        Denomination(500, CountryCode.PHP),
        Denomination(1000, CountryCode.PHP)
    ),
    /**
     * Maximum number of banknotes that fit into the cashbox
     */
    val cashboxCapacity: Int = 300,
    /**
     * Interval at which a Poll command will be sent to the BNA
     *
     * Format: Kotlin Duration format
     * Default: "1s"
     */
    val pollInterval: Duration = 1.seconds,
    /**
     * Interval at which a Hold command will be sent to the BNA when a banknote is in escrow
     *
     * Format: Kotlin Duration format
     * Default: "4.5s"
     */
    val holdInterval: Duration = 1.seconds,
    /**
     * Maximum load between collections in Peso
     *
     * Format: integer
     * Default: "30000"
     */
    val maxLoadBetweenCollections: Int = 30_000,
    /**
     * Flag whether a Reset command should be sent to the BNA on attachment
     *
     * Format: "true"/"false"
     * Default: "false"
     */
    val resetOnAttachment: Boolean = false
) {

    companion object {

        /**
         * USB configs for CBA9
         */
        var usbProps: UsbDriverProperties = UsbDriverProperties (
            names = listOf("Cba9","cba9","CBA9","CBA9SP"),
            vendorId = 6428,
            productId = 16_644,
            sendingEndpointIndex = 1,
            receivingEndpointIndex = 2,
            interfaceIndex = 1,
            controlInterfaceIndex = 0,
            timeoutSend = 2.seconds,
            timeoutReceive = 2.seconds,
            timeoutControlTransfer = Duration.ZERO,
            transferMode = UsbTransferMode.BULK_TIMEOUT
        )
    }
}

fun UsbDevice.isCba9(): Boolean = Cba9Properties.usbProps.productId == productId && Cba9Properties.usbProps.vendorId == vendorId
package com.africopay.pos.hal.interfaces

import com.africopay.pos.domain.model.QrResult
import com.africopay.pos.domain.model.ScanResult
import kotlinx.coroutines.flow.Flow

/**
 * Hardware Abstraction Layer interface for barcode/QR code scanner.
 */
interface ScannerService {
    /** Returns true if a dedicated barcode scanner is physically present. */
    fun isAvailable(): Boolean

    /**
     * Starts scanning for barcodes and QR codes.
     * Emits [ScanResult] when a code is detected.
     */
    fun startScanning(): Flow<ScanResult>

    /** Stops the scanner. */
    fun stopScanning()
}

/**
 * Hardware Abstraction Layer interface for camera-based QR scanning.
 */
interface CameraService {
    /** Returns true if a camera is available for QR scanning. */
    fun isAvailable(): Boolean

    /**
     * Starts camera-based QR code scanning.
     * Emits [QrResult] when a QR code is successfully decoded.
     */
    fun startQrScanning(): Flow<QrResult>

    /** Stops camera scanning. */
    fun stopScanning()
}

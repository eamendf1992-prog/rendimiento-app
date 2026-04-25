package com.utec.munainteractive.device

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.util.Log

class BleScanner(
    bluetoothAdapter: BluetoothAdapter,
    private val onDeviceFound: (String) -> Unit
) {
    private val scanner = bluetoothAdapter.bluetoothLeScanner
    private val processor = SignalProcessor()

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val deviceAddress = result.device.address
            val rawRssi = result.rssi

            // --- NUEVA LÓGICA PARA UUID ---
            val scanRecord = result.scanRecord?.bytes
            val beaconUuid = scanRecord?.let { extractIBeaconUuid(it) } ?: "N/A"
            // ------------------------------

            val cleanRssi = processor.smoothRssi(rawRssi)
            val distance = processor.calculateDistance(cleanRssi)

            // Logcat actualizado para mostrar UUID y MAC
            Log.d("BLE_DEBUG", """
                MAC: $deviceAddress
                UUID: $beaconUuid
                RSSI Crudo: $rawRssi | Filtrado: ${"%.2f".format(cleanRssi)} 
                Distancia: ${"%.2f".format(distance)} m
                ------------------------------------------
            """.trimIndent())

            if (distance <= 1.5) {
                // Priorizamos el UUID si se detectó, si no usamos la MAC
                val finalId = if (beaconUuid != "N/A") beaconUuid else deviceAddress
                Log.i("BLE_DEBUG", "¡OBJETO CERCA! Activando ID: $finalId")
                onDeviceFound(finalId)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.e("BLE_DEBUG", "Error en el escaneo: $errorCode")
        }
    }

    /**
     * Extrae el UUID de un paquete iBeacon estándar (Bytes 9 al 24)
     */
    private fun extractIBeaconUuid(scanRecord: ByteArray): String? {
        try {
            // Un paquete iBeacon válido tiene 0x02 y 0x15 en estas posiciones
            if (scanRecord.size > 26 && scanRecord[7].toInt() == 0x02 && scanRecord[8].toInt() == 0x15) {
                val b = scanRecord.sliceArray(9..24)
                // Formateamos a String hexadecimal con guiones (formato UUID estándar)
                return String.format(
                    "%02X%02X%02X%02X-%02X%02X-%02X%02X-%02X%02X-%02X%02X%02X%02X%02X%02X",
                    b[0], b[1], b[2], b[3], b[4], b[5], b[6], b[7], b[8], b[9], b[10], b[11], b[12], b[13], b[14], b[15]
                )
            }
        } catch (e: Exception) {
            Log.e("BLE_DEBUG", "Error parseando UUID: ${e.message}")
        }
        return null
    }

    @SuppressLint("MissingPermission")
    fun startScanning() {
        Log.d("BLE_DEBUG", "Iniciando escaneo BLE...")
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        scanner?.startScan(null, settings, scanCallback)
    }

    @SuppressLint("MissingPermission")
    fun stopScanning() {
        Log.d("BLE_DEBUG", "Deteniendo escaneo BLE.")
        scanner?.stopScan(scanCallback)
    }
}
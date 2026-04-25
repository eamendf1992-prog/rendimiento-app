package com.utec.munainteractive.device

import kotlin.math.pow

class SignalProcessor(
    private val alpha: Double = 0.4, // Factor de suavizado (entre 0 y 1)
    private val txPower: Int = -59,  // Pt: Potencia medida a 1 metro
    private val n: Double = 3.2      // Factor de atenuación ambiental
) {
    private var lastSmoothedRssi: Double? = null

    /**
     * Aplica la fórmula: St = α * Xt + (1 - α) * St-1
     */
    fun smoothRssi(currentRssi: Int): Double {
        val stMinus1 = lastSmoothedRssi ?: currentRssi.toDouble()
        val st = alpha * currentRssi + (1 - alpha) * stMinus1
        lastSmoothedRssi = st
        return st
    }

    /**
     * Aplica la fórmula: d = 10 ^ ((Pt - Pr) / (10 * n))
     */
    fun calculateDistance(rssi: Double): Double {
        val exponent = (txPower - rssi) / (10.0 * n)
        return 10.0.pow(exponent)
    }
}
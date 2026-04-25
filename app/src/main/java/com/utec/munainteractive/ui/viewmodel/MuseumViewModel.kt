package com.utec.munainteractive.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utec.munainteractive.data.model.MuseumObject
import com.utec.munainteractive.data.repository.MuseumRepository
import kotlinx.coroutines.launch

class MuseumViewModel : ViewModel() {
    private val repository = MuseumRepository()

    var detectedObject by mutableStateOf<MuseumObject?>(null)
    var showSheet by mutableStateOf(false)

    // Almacenamos el último UID que el usuario CERRÓ manualmente
    private var lastDismissedUid: String? = null

    fun onBeaconDetected(uid: String) {
        // REGLA 1: Si la tarjeta ya está abierta, no hagas nada
        if (showSheet) return

        // REGLA 2: Si el usuario acaba de cerrar ESTE mismo objeto, no lo vuelvas a abrir
        if (uid == lastDismissedUid) return

        viewModelScope.launch {
            val result = repository.getObjectByUid(uid)
            if (result != null) {
                detectedObject = result
                showSheet = true
                // Limpiamos el último descartado porque estamos ante uno nuevo o válido
                lastDismissedUid = null
            }
        }
    }

    fun dismissSheet() {
        // Cuando el usuario cierra la tarjeta, guardamos el UID para "bloquearlo"
        lastDismissedUid = detectedObject?.uid
        showSheet = false
    }
}
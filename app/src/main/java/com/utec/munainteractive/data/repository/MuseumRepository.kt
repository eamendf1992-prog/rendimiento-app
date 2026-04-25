package com.utec.munainteractive.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.utec.munainteractive.data.model.MuseumObject
import kotlinx.coroutines.tasks.await

class MuseumRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun getObjectByUid(uid: String): MuseumObject? {
        return try {
            val snapshot = db.collection("articulos")
                .whereEqualTo("uid", uid)
                .get()
                .await()

            // Retorna el primer objeto encontrado o null
            snapshot.documents.firstOrNull()?.toObject(MuseumObject::class.java)
        } catch (e: Exception) {
            null
        }
    }
}
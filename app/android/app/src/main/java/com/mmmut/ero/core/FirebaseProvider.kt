package com.mmmut.ero.core

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage

object FirebaseProvider {
    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance().apply {
            try {
                firestoreSettings = FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true).build()
            } catch (_: Exception) { }
        }
    }
    val messaging: FirebaseMessaging by lazy { FirebaseMessaging.getInstance() }
    val storage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }
}

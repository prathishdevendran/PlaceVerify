package com.example.placeverify

import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions

object FirebaseEnvironment {
    private var configured = false
    fun configure() {
        if (configured) return
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(DebugAppCheckProviderFactory.getInstance())
        if (BuildConfig.USE_FIREBASE_EMULATORS) {
        FirebaseAuth.getInstance().useEmulator("10.0.2.2", 9099)
        FirebaseFirestore.getInstance().useEmulator("10.0.2.2", 8080)
        FirebaseFunctions.getInstance("us-central1").useEmulator("10.0.2.2", 5001)
        }
        configured = true
    }
}

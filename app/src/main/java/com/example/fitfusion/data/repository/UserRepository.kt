package com.example.fitfusion.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.lang.Exception

class UserRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    fun createUserProfile(
        uid: String,
        email: String,
        displayName: String,
        photoUrl: String? = null,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit,
    ) {
        val userDocument = hashMapOf<String, Any?>(
            "email" to email,
            "displayName" to displayName,
            "createdAt" to FieldValue.serverTimestamp(),
            "photoUrl" to photoUrl,
            "heightCm" to null,
            "weightKg" to null,
            "goalType" to null,
            "activityLevel" to null,
        )

        firestore.collection("users")
            .document(uid)
            .set(userDocument)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onError(exception) }
    }
}

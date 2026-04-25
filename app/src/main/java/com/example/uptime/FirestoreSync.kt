package com.example.uptime

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FirestoreSync {

    private val db = FirebaseFirestore.getInstance()

    private fun getUserId(): String? = FirebaseAuth.getInstance().currentUser?.uid

    // save any data under the current user's document
    fun saveUserData(data: Map<String, Any>) {
        val uid = getUserId() ?: return
        db.collection("users").document(uid).set(data, com.google.firebase.firestore.SetOptions.merge())
    }

    // save to a subcollection under the user
    fun saveSubcollection(subcollection: String, docId: String, data: Map<String, Any>) {
        val uid = getUserId() ?: return
        db.collection("users").document(uid)
            .collection(subcollection).document(docId).set(data)
    }

    // read another user's data by their userId
    fun getUserData(userId: String, onResult: (Map<String, Any>?) -> Unit) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { doc -> onResult(doc.data) }
            .addOnFailureListener { onResult(null) }
    }
}

package com.example.uptime.room

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseRoomRepository {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getRoomSettings(userId: String): RoomSettings? {
        return try {
            val doc = firestore.collection("users")
                .document(userId)
                .collection("room")
                .document("settings")
                .get()
                .await()

            doc.toObject(RoomSettingsConverted::class.java)?.unconvert()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getUserInventory(userId: String): UserInventory? {
        return try {
            val doc = firestore.collection("users")
                .document(userId)
                .collection("room")
                .document("inventory")
                .get()
                .await()

            doc.toObject(UserInventoryConverted::class.java)?.unconvert()
        } catch (e: Exception) {
            null
        }
    }

    fun observeRoomSettings(userId: String): Flow<RoomSettings?> = callbackFlow {
        val listener = firestore.collection("users")
            .document(userId)
            .collection("room")
            .document("settings")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObject(RoomSettingsConverted::class.java)?.unconvert())
            }
        awaitClose { listener.remove() }
    }

    fun observeUserInventory(userId: String): Flow<UserInventory?> = callbackFlow {
        val listener = firestore.collection("users")
            .document(userId)
            .collection("room")
            .document("inventory")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObject(UserInventoryConverted::class.java)?.unconvert())
            }
        awaitClose { listener.remove() }
    }

    // Write current user's data to Firestore so others can read it
    suspend fun syncRoomSettings(userId: String, settings: RoomSettings) {
        try {
            val batch = firestore.batch()

            val userRef = firestore.collection("users").document(userId)
            val settingsRef = userRef.collection("room").document("settings")

            batch.set(userRef, mapOf("lastActivity" to FieldValue.serverTimestamp()), SetOptions.merge())
            batch.set(settingsRef, settings.convert())

            batch.commit().await()
        } catch (e: Exception) {
            Log.e("FirebaseRoom", "Failed to sync room settings", e)
        }
    }

    suspend fun syncInventory(userId: String, inventory: UserInventory) {
        try {
            val batch = firestore.batch()

            val userRef = firestore.collection("users").document(userId)
            val inventoryRef = userRef.collection("room").document("inventory")

            batch.set(userRef, mapOf("lastActivity" to FieldValue.serverTimestamp()), SetOptions.merge())
            batch.set(inventoryRef, inventory.convert())

            batch.commit().await()
        } catch (e: Exception) {
            Log.e("FirebaseRoom", "Failed to sync inventory", e)
        }
    }


    suspend fun getRandomUserId(excludeUserId: String? = null): String? {
        return try {
            val snapshot = firestore.collection("users")
                .whereNotEqualTo("email", null)
                .get() // may want to limit later if this collection gets too big
                .await()

            val userIds = snapshot.documents
                .map { it.id }
                .filter { it != excludeUserId }

            if (userIds.isEmpty()) return null

            userIds.random()
        } catch (e: Exception) {
            null
        }
    }
}
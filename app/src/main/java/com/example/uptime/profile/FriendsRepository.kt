package com.example.uptime.profile

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

data class FriendProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val streak: Int = 0,
    val trophies: Int = 0
)

class FriendsRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun currentUid(): String? = auth.currentUser?.uid

    suspend fun saveUserProfile(name: String, email: String, streak: Int, trophies: Int) {
        val uid = currentUid() ?: return
        try {
            val existing = getFriendIds() ?: emptyList<String>()
            db.collection("users").document(uid).set(
                mapOf(
                    "name" to name,
                    "email" to email,
                    "streak" to streak,
                    "trophies" to trophies,
                    "friends" to existing
                ),
                SetOptions.merge()
            ).await()
        } catch (_: Exception) {
            db.collection("users").document(uid).set(
                mapOf(
                    "name" to name,
                    "email" to email,
                    "streak" to streak,
                    "trophies" to trophies,
                    "friends" to emptyList<String>()
                )
            ).await()
        }
    }

    suspend fun updateName(name: String) {
        val uid = currentUid() ?: return
        db.collection("users").document(uid).update("name", name).await()
    }

    suspend fun syncStats(streak: Int, trophies: Int) {
        val uid = currentUid() ?: return
        db.collection("users").document(uid).update(
            mapOf("streak" to streak, "trophies" to trophies)
        ).await()
    }

    suspend fun addFriendByEmail(email: String): Result<String> {
        val uid = currentUid() ?: return Result.failure(Exception("Not signed in"))

        val snapshot = db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .await()

        if (snapshot.isEmpty) return Result.failure(Exception("No user found with that email"))

        val friendDoc = snapshot.documents.first()
        val friendUid = friendDoc.id

        if (friendUid == uid) return Result.failure(Exception("Can't add yourself"))

        val myFriends = getFriendIds()?.toMutableList() ?: mutableListOf()
        if (friendUid in myFriends) return Result.failure(Exception("Already friends"))
        myFriends.add(friendUid)
        db.collection("users").document(uid).update("friends", myFriends).await()

        val theirFriends = getFriendIdsFor(friendUid)?.toMutableList() ?: mutableListOf()
        if (uid !in theirFriends) {
            theirFriends.add(uid)
            db.collection("users").document(friendUid).update("friends", theirFriends).await()
        }

        return Result.success(friendUid)
    }

    suspend fun addFriendById(userId: String): Result<String> {
        val uid = currentUid() ?: return Result.failure(Exception("Not signed in"))

        if (userId == uid) return Result.failure(Exception("Can't add yourself"))

        Log.d("AddFriend", "Trying to add: $userId")

        val friendDoc = db.collection("users")
            .document(userId)
            .get()
            .await()

        if (!friendDoc.exists()) {
            return Result.failure(Exception("No user found with that ID"))
        }

        val myFriends = getFriendIds()?.toMutableList() ?: mutableListOf()
        if (userId in myFriends) return Result.failure(Exception("Already friends"))

        myFriends.add(userId)
        db.collection("users").document(uid)
            .update("friends", myFriends)
            .await()

        val theirFriends = getFriendIdsFor(userId)?.toMutableList() ?: mutableListOf()
        if (uid !in theirFriends) {
            theirFriends.add(uid)
            db.collection("users").document(userId)
                .update("friends", theirFriends)
                .await()
        }

        return Result.success(userId)
    }

    suspend fun removeFriend(friendUid: String) {
        val uid = currentUid() ?: return

        val myFriends = getFriendIds()?.toMutableList() ?: return
        myFriends.remove(friendUid)
        db.collection("users").document(uid).update("friends", myFriends).await()

        val theirFriends = getFriendIdsFor(friendUid)?.toMutableList() ?: return
        theirFriends.remove(uid)
        db.collection("users").document(friendUid).update("friends", theirFriends).await()
    }

    fun observeFriends(): Flow<List<FriendProfile>> = callbackFlow {
        val uid = currentUid()
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = db.collection("users").document(uid)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot == null) return@addSnapshotListener

                val friendIds = snapshot.get("friends") as? List<*> ?: emptyList<String>()
                if (friendIds.isEmpty()) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                db.collection("users")
                    .whereIn(
                        FieldPath.documentId(),
                        friendIds.map { it.toString() })
                    .get()
                    .addOnSuccessListener { friendDocs ->
                        val friends = friendDocs.map { doc ->
                            FriendProfile(
                                uid = doc.id,
                                name = doc.getString("name") ?: "",
                                email = doc.getString("email") ?: "",
                                streak = (doc.getLong("streak") ?: 0).toInt(),
                                trophies = (doc.getLong("trophies") ?: 0).toInt()
                            )
                        }
                        trySend(friends)
                    }
            }

        awaitClose { listener.remove() }
    }

    // get current user's name from Firestore
    suspend fun getCurrentUserName(): String? {
        val uid = currentUid() ?: return null
        val doc = db.collection("users").document(uid).get().await()
        return doc.getString("name")
    }

    private suspend fun getFriendIds(): List<String>? {
        val uid = currentUid() ?: return null
        val doc = db.collection("users").document(uid).get().await()
        return doc.get("friends") as? List<String>
    }

    private suspend fun getFriendIdsFor(userId: String): List<String>? {
        val doc = db.collection("users").document(userId).get().await()
        return doc.get("friends") as? List<String>
    }
}
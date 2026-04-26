package com.example.uptime

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

data class FriendProfile(
    val uid: String = "",
    val email: String = "",
    val streak: Int = 0,
    val trophies: Int = 0
)

class FriendsRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun currentUid(): String? = auth.currentUser?.uid

    // save or update the current user's profile in Firestore
    suspend fun saveUserProfile(email: String, streak: Int, trophies: Int) {
        val uid = currentUid() ?: return
        db.collection("users").document(uid).set(
            mapOf(
                "email" to email,
                "streak" to streak,
                "trophies" to trophies,
                "friends" to (getFriendIds() ?: emptyList<String>())
            ),
            com.google.firebase.firestore.SetOptions.merge()
        ).await()
    }

    // update just the streak and trophies
    suspend fun syncStats(streak: Int, trophies: Int) {
        val uid = currentUid() ?: return
        db.collection("users").document(uid).update(
            mapOf("streak" to streak, "trophies" to trophies)
        ).await()
    }

    // find a user by email and add them as a friend
    suspend fun addFriendByEmail(email: String): Result<String> {
        val uid = currentUid() ?: return Result.failure(Exception("Not signed in"))

        // find user with that email
        val snapshot = db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .await()

        if (snapshot.isEmpty) return Result.failure(Exception("No user found with that email"))

        val friendDoc = snapshot.documents.first()
        val friendUid = friendDoc.id

        if (friendUid == uid) return Result.failure(Exception("Can't add yourself"))

        // add to my friends list
        val myFriends = getFriendIds()?.toMutableList() ?: mutableListOf()
        if (friendUid in myFriends) return Result.failure(Exception("Already friends"))
        myFriends.add(friendUid)
        db.collection("users").document(uid).update("friends", myFriends).await()

        // add me to their friends list
        val theirFriends = getFriendIdsFor(friendUid)?.toMutableList() ?: mutableListOf()
        if (uid !in theirFriends) {
            theirFriends.add(uid)
            db.collection("users").document(friendUid).update("friends", theirFriends).await()
        }

        return Result.success(friendUid)
    }

    // remove a friend
    suspend fun removeFriend(friendUid: String) {
        val uid = currentUid() ?: return

        // remove from my list
        val myFriends = getFriendIds()?.toMutableList() ?: return
        myFriends.remove(friendUid)
        db.collection("users").document(uid).update("friends", myFriends).await()

        // remove me from their list
        val theirFriends = getFriendIdsFor(friendUid)?.toMutableList() ?: return
        theirFriends.remove(uid)
        db.collection("users").document(friendUid).update("friends", theirFriends).await()
    }

    // get friend profiles as a Flow for live updates
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

                // fetch each friend's profile
                db.collection("users")
                    .whereIn(com.google.firebase.firestore.FieldPath.documentId(),
                        friendIds.map { it.toString() })
                    .get()
                    .addOnSuccessListener { friendDocs ->
                        val friends = friendDocs.map { doc ->
                            FriendProfile(
                                uid = doc.id,
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

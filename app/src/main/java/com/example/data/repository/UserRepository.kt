package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.UserProfile
import com.example.data.util.OperationType
import com.example.data.util.handleFirestoreError
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val usersCollection = firestore.collection("users")
    private val subscriptionsCollection = firestore.collection("subscriptions")

    fun observeUserProfile(userId: String): Flow<UserProfile?> {
        return usersCollection.document(userId).snapshots()
            .map { snapshot -> snapshot.toObject(UserProfile::class.java) }
            .catch { e ->
                if (e is Exception) {
                    handleFirestoreError(e, OperationType.GET, "users/$userId")
                }
                throw e
            }
    }

    suspend fun syncUserProfile(): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(IllegalStateException("Non connecté"))
        val docRef = usersCollection.document(user.uid)
        return try {
            val snapshot = docRef.get().await()
            val isAdmin = user.email == "bossokomlasimon0@gmail.com"
            if (!snapshot.exists()) {
                val profile = UserProfile(
                    userId = user.uid,
                    email = user.email ?: "",
                    displayName = user.displayName ?: "Créateur Bright Video",
                    avatarUrl = user.photoUrl?.toString() ?: "",
                    role = if (isAdmin) "admin" else "user",
                    subscriberCount = 0L
                )
                val payload = profile.toMap().toMutableMap().apply {
                    put("createdAt", FieldValue.serverTimestamp())
                    put("updatedAt", FieldValue.serverTimestamp())
                }
                docRef.set(payload).await()
            } else {
                val existingRole = snapshot.getString("role") ?: "user"
                val resolvedRole = if (isAdmin) "admin" else existingRole
                docRef.update(
                    mapOf(
                        "displayName" to (user.displayName ?: "Créateur Bright Video"),
                        "avatarUrl" to (user.photoUrl?.toString() ?: ""),
                        "role" to resolvedRole,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateBio(bio: String): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Non connecté"))
        val docRef = usersCollection.document(uid)
        return try {
            docRef.update(
                mapOf(
                    "bio" to bio.take(500),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    fun observeIsSubscribed(channelUserId: String): Flow<Boolean> = callbackFlow {
        val subscriberId = auth.currentUser?.uid
        if (subscriberId == null) {
            trySend(false)
            close()
            return@callbackFlow
        }
        val subDocId = "${subscriberId}_$channelUserId"
        val docRef = subscriptionsCollection.document(subDocId)

        val reg = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.GET, docRef.path)
                trySend(false)
                return@addSnapshotListener
            }
            trySend(snapshot != null && snapshot.exists())
        }
        awaitClose { reg.remove() }
    }

    suspend fun toggleSubscription(channelUserId: String, channelName: String): Result<Boolean> {
        val subscriberId = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Non connecté"))

        if (subscriberId == channelUserId) {
            return Result.failure(IllegalArgumentException("Impossible de s'abonner à sa propre chaîne"))
        }

        val subDocId = "${subscriberId}_$channelUserId"
        val docRef = subscriptionsCollection.document(subDocId)

        return try {
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                docRef.delete().await()
                usersCollection.document(channelUserId).update(
                    "subscriberCount", FieldValue.increment(-1),
                    "updatedAt", FieldValue.serverTimestamp()
                ).await()
                Result.success(false)
            } else {
                docRef.set(
                    mapOf(
                        "subscriberId" to subscriberId,
                        "channelUserId" to channelUserId,
                        "channelName" to channelName,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                usersCollection.document(channelUserId).update(
                    "subscriberCount", FieldValue.increment(1),
                    "updatedAt", FieldValue.serverTimestamp()
                ).await()
                Result.success(true)
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }
}

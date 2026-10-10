package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.Comment
import com.example.data.model.Publication
import com.example.data.model.Report
import com.example.data.util.OperationType
import com.example.data.util.handleFirestoreError
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class PublicationRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val publicationsCollection = firestore.collection("publications")
    private val reportsCollection = firestore.collection("reports")

    fun currentUserId(): String? = auth.currentUser?.uid

    fun isUserAdmin(): Boolean {
        val email = auth.currentUser?.email
        return email == "bossokomlasimon0@gmail.com"
    }

    /**
     * Observers approved publications for the public feed, optionally filtered by category or type.
     */
    fun observeApprovedPublications(
        category: String? = null,
        type: String? = null
    ): Flow<List<Publication>> {
        var query: Query = publicationsCollection
            .whereEqualTo("status", "approved")
            .orderBy("createdAt", Query.Direction.DESCENDING)

        if (!category.isNullOrBlank() && category != "Tous") {
            query = query.whereEqualTo("category", category)
        }
        if (!type.isNullOrBlank() && type != "all") {
            query = query.whereEqualTo("type", type)
        }

        return query.snapshots()
            .map { snapshot -> snapshot.toObjects(Publication::class.java) }
            .catch { e ->
                if (e is Exception) {
                    handleFirestoreError(e, OperationType.LIST, "publications")
                }
                throw e
            }
    }

    /**
     * Observers publications authored by the current user.
     */
    fun observeUserPublications(userId: String): Flow<List<Publication>> {
        return publicationsCollection
            .whereEqualTo("authorId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot -> snapshot.toObjects(Publication::class.java) }
            .catch { e ->
                if (e is Exception) {
                    handleFirestoreError(e, OperationType.LIST, "publications?authorId=$userId")
                }
                throw e
            }
    }

    /**
     * Admin: Observe all pending publications for moderation.
     */
    fun observePendingPublications(): Flow<List<Publication>> {
        return publicationsCollection
            .whereEqualTo("status", "pending")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .snapshots()
            .map { snapshot -> snapshot.toObjects(Publication::class.java) }
            .catch { e ->
                if (e is Exception) {
                    handleFirestoreError(e, OperationType.LIST, "publications?status=pending")
                }
                throw e
            }
    }

    /**
     * Observe single publication details.
     */
    fun observePublication(publicationId: String): Flow<Publication?> {
        return publicationsCollection.document(publicationId).snapshots()
            .map { snapshot -> snapshot.toObject(Publication::class.java) }
            .catch { e ->
                if (e is Exception) {
                    handleFirestoreError(e, OperationType.GET, "publications/$publicationId")
                }
                throw e
            }
    }

    /**
     * Creates a new publication.
     * Regular users submit as 'pending'. Admin submissions can be 'approved'.
     */
    suspend fun createPublication(publication: Publication): Result<String> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Utilisateur non connecté"))

        val isAdmin = isUserAdmin()
        val statusToSet = if (isAdmin) "approved" else "pending"

        val payload = publication.toMap().toMutableMap().apply {
            put("authorId", uid)
            put("status", statusToSet)
            put("createdAt", FieldValue.serverTimestamp())
            put("updatedAt", FieldValue.serverTimestamp())
        }

        return try {
            val docRef = publicationsCollection.document()
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "publications")
            Result.failure(e)
        }
    }

    /**
     * Moderation action: Approve or Reject publication.
     */
    suspend fun moderatePublication(
        publicationId: String,
        approved: Boolean,
        reason: String = ""
    ): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>(
                "status" to (if (approved) "approved" else "rejected"),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (!approved && reason.isNotBlank()) {
                updates["rejectionReason"] = reason
            }
            publicationsCollection.document(publicationId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "publications/$publicationId")
            Result.failure(e)
        }
    }

    /**
     * Delete publication (author or admin).
     */
    suspend fun deletePublication(publicationId: String): Result<Unit> {
        return try {
            publicationsCollection.document(publicationId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "publications/$publicationId")
            Result.failure(e)
        }
    }

    /**
     * Increment views counter.
     */
    suspend fun incrementViews(publicationId: String) {
        try {
            publicationsCollection.document(publicationId).update(
                mapOf(
                    "viewsCount" to FieldValue.increment(1),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: Exception) {
            // Silently log view error
            handleFirestoreError(e, OperationType.UPDATE, "publications/$publicationId")
        }
    }

    /**
     * Observe comments of a publication.
     */
    fun observeComments(publicationId: String): Flow<List<Comment>> {
        return publicationsCollection.document(publicationId)
            .collection("comments")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot -> snapshot.toObjects(Comment::class.java) }
            .catch { e ->
                if (e is Exception) {
                    handleFirestoreError(e, OperationType.LIST, "publications/$publicationId/comments")
                }
                throw e
            }
    }

    /**
     * Add a comment.
     */
    suspend fun addComment(publicationId: String, content: String): Result<String> {
        val user = auth.currentUser
            ?: return Result.failure(IllegalStateException("Connexion requise"))

        val commentRef = publicationsCollection.document(publicationId)
            .collection("comments").document()

        val payload = mapOf(
            "publicationId" to publicationId,
            "authorId" to user.uid,
            "authorName" to (user.displayName ?: "Abonné"),
            "authorAvatarUrl" to (user.photoUrl?.toString() ?: ""),
            "content" to content.trim(),
            "createdAt" to FieldValue.serverTimestamp()
        )

        return try {
            commentRef.set(payload).await()
            // Increment comment count
            publicationsCollection.document(publicationId).update(
                "commentsCount", FieldValue.increment(1),
                "updatedAt", FieldValue.serverTimestamp()
            ).await()
            Result.success(commentRef.id)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "publications/$publicationId/comments")
            Result.failure(e)
        }
    }

    /**
     * Check if user liked this publication.
     */
    fun observeIsLiked(publicationId: String, userId: String): Flow<Boolean> = callbackFlow {
        val docRef = publicationsCollection.document(publicationId)
            .collection("likes").document(userId)

        val registration = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.GET, docRef.path)
                trySend(false)
                return@addSnapshotListener
            }
            trySend(snapshot != null && snapshot.exists())
        }

        awaitClose { registration.remove() }
    }

    /**
     * Toggle like state.
     */
    suspend fun toggleLike(publicationId: String): Result<Boolean> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Connexion requise"))

        val likeDoc = publicationsCollection.document(publicationId)
            .collection("likes").document(uid)

        return try {
            val snapshot = likeDoc.get().await()
            if (snapshot.exists()) {
                likeDoc.delete().await()
                publicationsCollection.document(publicationId).update(
                    "likesCount", FieldValue.increment(-1),
                    "updatedAt", FieldValue.serverTimestamp()
                ).await()
                Result.success(false)
            } else {
                likeDoc.set(
                    mapOf(
                        "userId" to uid,
                        "publicationId" to publicationId,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                publicationsCollection.document(publicationId).update(
                    "likesCount", FieldValue.increment(1),
                    "updatedAt", FieldValue.serverTimestamp()
                ).await()
                Result.success(true)
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, likeDoc.path)
            Result.failure(e)
        }
    }

    /**
     * Submit a report for moderation.
     */
    suspend fun submitReport(
        publicationId: String,
        publicationTitle: String,
        reason: String,
        details: String
    ): Result<Unit> {
        val user = auth.currentUser
            ?: return Result.failure(IllegalStateException("Connexion requise"))

        val payload = mapOf(
            "publicationId" to publicationId,
            "publicationTitle" to publicationTitle,
            "reportedByUserId" to user.uid,
            "reporterEmail" to (user.email ?: ""),
            "reason" to reason,
            "details" to details,
            "status" to "pending",
            "createdAt" to FieldValue.serverTimestamp()
        )

        return try {
            reportsCollection.document().set(payload).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "reports")
            Result.failure(e)
        }
    }

    /**
     * Admin: Observe all reported items.
     */
    fun observeReports(): Flow<List<Report>> {
        return reportsCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot -> snapshot.toObjects(Report::class.java) }
            .catch { e ->
                if (e is Exception) {
                    handleFirestoreError(e, OperationType.LIST, "reports")
                }
                throw e
            }
    }

    /**
     * Admin: Update report resolution status.
     */
    suspend fun updateReportStatus(reportId: String, newStatus: String): Result<Unit> {
        return try {
            reportsCollection.document(reportId).update(
                "status", newStatus,
                "updatedAt", FieldValue.serverTimestamp()
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "reports/$reportId")
            Result.failure(e)
        }
    }
}

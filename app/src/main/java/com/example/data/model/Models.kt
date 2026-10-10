package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class PublicationType(val id: String, val label: String, val iconName: String) {
    VIDEO("video", "Vidéos", "smart_display"),
    TUTORIAL("tutorial", "Tutoriels", "school"),
    ARTICLE("article", "Articles", "article"),
    MUSIC("music", "Musique", "headphones"),
    DOWNLOAD("download", "Téléchargements", "download"),
    DOCUMENT("document", "Documents", "description"),
    PHOTO("photo", "Photos", "photo_camera");

    companion object {
        fun fromId(id: String): PublicationType {
            return entries.find { it.id == id } ?: VIDEO
        }
    }
}

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val email: String = "",
    val displayName: String = "",
    val avatarUrl: String = "",
    val bio: String = "",
    val role: String = "user", // "user" or "admin"
    val subscriberCount: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "userId" to userId,
            "displayName" to displayName,
            "role" to role,
            "subscriberCount" to subscriberCount
        )
        if (email.isNotBlank()) map["email"] = email
        if (avatarUrl.isNotBlank()) map["avatarUrl"] = avatarUrl
        if (bio.isNotBlank()) map["bio"] = bio
        return map
    }
}

@IgnoreExtraProperties
data class Publication(
    @DocumentId val id: String = "",
    val title: String = "",
    val description: String = "",
    val type: String = "video",
    val category: String = "Technologie",
    val externalUrl: String = "",
    val thumbnailUrl: String = "",
    val content: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorAvatarUrl: String = "",
    val status: String = "pending", // "pending", "approved", "rejected"
    val rejectionReason: String = "",
    val viewsCount: Long = 0L,
    val likesCount: Long = 0L,
    val commentsCount: Long = 0L,
    val tags: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "title" to title,
            "type" to type,
            "category" to category,
            "externalUrl" to externalUrl,
            "authorId" to authorId,
            "status" to status,
            "viewsCount" to viewsCount,
            "likesCount" to likesCount,
            "commentsCount" to commentsCount,
            "tags" to tags
        )
        if (description.isNotBlank()) map["description"] = description
        if (thumbnailUrl.isNotBlank()) map["thumbnailUrl"] = thumbnailUrl
        if (content.isNotBlank()) map["content"] = content
        if (authorName.isNotBlank()) map["authorName"] = authorName
        if (authorAvatarUrl.isNotBlank()) map["authorAvatarUrl"] = authorAvatarUrl
        if (rejectionReason.isNotBlank()) map["rejectionReason"] = rejectionReason
        return map
    }
}

@IgnoreExtraProperties
data class Comment(
    @DocumentId val id: String = "",
    val publicationId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorAvatarUrl: String = "",
    val content: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "publicationId" to publicationId,
            "authorId" to authorId,
            "content" to content
        )
        if (authorName.isNotBlank()) map["authorName"] = authorName
        if (authorAvatarUrl.isNotBlank()) map["authorAvatarUrl"] = authorAvatarUrl
        return map
    }
}

@IgnoreExtraProperties
data class Like(
    @DocumentId val id: String = "",
    val userId: String = "",
    val publicationId: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Subscription(
    @DocumentId val id: String = "",
    val subscriberId: String = "",
    val channelUserId: String = "",
    val channelName: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Report(
    @DocumentId val id: String = "",
    val publicationId: String = "",
    val publicationTitle: String = "",
    val reportedByUserId: String = "",
    val reporterEmail: String = "",
    val reason: String = "",
    val details: String = "",
    val status: String = "pending", // "pending", "resolved", "dismissed"
    @ServerTimestamp val createdAt: Timestamp? = null
)

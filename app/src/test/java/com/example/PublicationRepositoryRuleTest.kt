package com.example

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.Publication
import com.example.data.repository.PublicationRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PublicationRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createPublication_authenticatedUser_success() = runBlocking {
        val uid = signInTestUser("creator@brightvideo.test")
        val repo = PublicationRepository(firestore, auth)

        val pub = Publication(
            title = "Super Guide Android Jetpack",
            type = "tutorial",
            category = "Tutoriels",
            externalUrl = "https://youtube.com/watch?v=sample123",
            description = "Un tutoriel complet pour maîtriser Jetpack Compose",
            authorId = uid,
            authorName = "Simon Creator",
            status = "pending"
        )

        val result = withTimeout(5000L) {
            repo.createPublication(pub)
        }

        assertTrue("Création de publication réussie", result.isSuccess)
        val pubId = result.getOrThrow()
        assertNotNull(pubId)
    }

    @Test
    fun observeUserPublications_returnsAuthoredPublications() = runBlocking {
        val uid = signInTestUser("user2@brightvideo.test")
        val repo = PublicationRepository(firestore, auth)

        val pub = Publication(
            title = "Chanson originale Bright Beats",
            type = "music",
            category = "Musique & Audio",
            externalUrl = "https://soundcloud.com/brightvideo/track",
            authorId = uid,
            authorName = "DJ Bright",
            status = "pending"
        )

        val pubId = withTimeout(5000L) { repo.createPublication(pub).getOrThrow() }

        val list = withTimeout(5000L) {
            repo.observeUserPublications(uid).first { it.any { p -> p.id == pubId } }
        }

        assertTrue("La liste contient la publication de l'auteur", list.any { it.id == pubId })
    }

    @Test
    fun createPublication_unauthenticatedUser_fails() = runBlocking {
        auth.signOut()
        val repo = PublicationRepository(firestore, auth)

        val pub = Publication(
            title = "Non authentifié",
            type = "video",
            category = "Technologie",
            externalUrl = "https://youtube.com/watch?v=fails",
            authorId = "unknown",
            status = "pending"
        )

        val result = repo.createPublication(pub)
        assertTrue("La création échoue si non authentifié", result.isFailure)
    }
}

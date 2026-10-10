package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import com.example.data.model.Publication
import com.example.data.repository.PublicationRepository
import com.example.data.repository.UserRepository
import com.example.ui.auth.AuthManager
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.LoginScreen
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.BrightVideoTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BrightVideoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BrightVideoApp()
                }
            }
        }
    }
}

private const val ROUTE_LOGIN = "login"
private const val ROUTE_MAIN = "main"

@Composable
fun BrightVideoApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val auth = remember { FirebaseAuth.getInstance() }
    val credentialManager = remember { CredentialManager.create(context) }
    val navController = rememberNavController()

    var currentUser by remember { mutableStateOf<FirebaseUser?>(auth.currentUser) }
    var isCheckingAuth by remember { mutableStateOf(auth.currentUser == null) }

    val publicationRepository = remember { PublicationRepository(context) }
    val userRepository = remember { UserRepository(context) }

    // Auth state listener to keep navigation in sync
    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            currentUser = user
            if (user != null) {
                isCheckingAuth = false
                scope.launch {
                    userRepository.syncUserProfile()
                }
            }
        }
        auth.addAuthStateListener(listener)
        onDispose {
            auth.removeAuthStateListener(listener)
        }
    }

    // Attempt silent auto-sign in on cold launch if no user
    LaunchedEffect(Unit) {
        if (auth.currentUser == null) {
            AuthManager.attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = {
                    val user = auth.currentUser
                    currentUser = user
                    isCheckingAuth = false
                    navController.navigate(ROUTE_MAIN) {
                        popUpTo(ROUTE_LOGIN) { inclusive = true }
                    }
                },
                onUnauthenticated = {
                    isCheckingAuth = false
                },
                scope = scope
            )
        } else {
            isCheckingAuth = false
            seedInitialContentIfEmpty(publicationRepository)
        }
    }

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            seedInitialContentIfEmpty(publicationRepository)
        }
    }

    if (isCheckingAuth) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    } else {
        val startDestination = if (currentUser != null) ROUTE_MAIN else ROUTE_LOGIN

        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(ROUTE_LOGIN) {
                LoginScreen(
                    onAuthSuccess = {
                        currentUser = auth.currentUser
                        navController.navigate(ROUTE_MAIN) {
                            popUpTo(ROUTE_LOGIN) { inclusive = true }
                        }
                    }
                )
            }

            composable(ROUTE_MAIN) {
                AppNavigation(
                    publicationRepository = publicationRepository,
                    userRepository = userRepository,
                    onSignOut = {
                        AuthManager.signOut(
                            context = context,
                            credentialManager = credentialManager,
                            onSignOutComplete = {
                                currentUser = null
                                navController.navigate(ROUTE_LOGIN) {
                                    popUpTo(ROUTE_MAIN) { inclusive = true }
                                }
                            },
                            scope = scope
                        )
                    }
                )
            }
        }
    }
}

/**
 * Seeds high-quality initial French sample publications if feed is empty
 */
private suspend fun seedInitialContentIfEmpty(repository: PublicationRepository) {
    try {
        val existing = repository.observeApprovedPublications().first()
        if (existing.isEmpty()) {
            val samples = listOf(
                Publication(
                    title = "Découvrir Jetpack Compose & Material 3 sur Android",
                    description = "Guide complet vidéo pour créer des interfaces mobiles modernes et réactives.",
                    type = "tutorial",
                    category = "Tutoriels",
                    externalUrl = "https://www.youtube.com/watch?v=12345",
                    thumbnailUrl = "https://images.unsplash.com/photo-1607252650355-f7fd0460ccdb?w=800&auto=format&fit=crop&q=80",
                    content = "Dans ce tutoriel pas à pas, découvrez comment structurer votre architecture Android en MVVM avec Kotlin Coroutines, StateFlow et Material 3.\n\nPoints abordés :\n1. Composants Scaffold et TopAppBar\n2. Gestion de l'état avec ViewModel\n3. Thème dynamique et mode sombre.",
                    authorName = "Équipe Bright Video",
                    status = "approved",
                    viewsCount = 142L,
                    likesCount = 28L,
                    commentsCount = 4L,
                    tags = listOf("android", "compose", "kotlin", "tutoriel")
                ),
                Publication(
                    title = "Cinématique 4K : Les Merveilles de la Nature",
                    description = "Magnifique documentaire cinématique 4K filmé au drone dans les Alpes.",
                    type = "video",
                    category = "Vidéos & Cinéma",
                    externalUrl = "https://www.youtube.com/watch?v=sampleNature",
                    thumbnailUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800&auto=format&fit=crop&q=80",
                    authorName = "Studio Horizon",
                    status = "approved",
                    viewsCount = 310L,
                    likesCount = 57L,
                    commentsCount = 8L,
                    tags = listOf("video", "4k", "nature", "drone")
                ),
                Publication(
                    title = "Pack d'Icônes & UI Kit pour Développeurs",
                    description = "Téléchargez gratuitement plus de 500 icônes vectorielles prêtes à l'emploi.",
                    type = "download",
                    category = "Téléchargements",
                    externalUrl = "https://github.com/google/material-design-icons",
                    thumbnailUrl = "https://images.unsplash.com/photo-1581291518857-4e27b48ff24e?w=800&auto=format&fit=crop&q=80",
                    content = "Ce pack open-source contient des icônes SVG optimisées pour les applications mobiles et sites web.\n\nFormats inclus :\n- SVG haute résolution\n- Fichiers VectorDrawable pour Android Studio\n- Licence libre d'utilisation commerciale.",
                    authorName = "DesignLab",
                    status = "approved",
                    viewsCount = 215L,
                    likesCount = 44L,
                    commentsCount = 3L,
                    tags = listOf("ressources", "svg", "design", "gratuit")
                ),
                Publication(
                    title = "Session Lo-Fi Chillout & Beats Électroniques",
                    description = "Ambiance sonore relaxante idéale pour travailler, coder ou étudier.",
                    type = "music",
                    category = "Musique & Audio",
                    externalUrl = "https://soundcloud.com/chillhopdotcom",
                    thumbnailUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=800&auto=format&fit=crop&q=80",
                    authorName = "DJ ChillWaves",
                    status = "approved",
                    viewsCount = 480L,
                    likesCount = 89L,
                    commentsCount = 12L,
                    tags = listOf("musique", "lofi", "chill", "focus")
                ),
                Publication(
                    title = "Guide RGPD et Cybersécurité pour Applications Web",
                    description = "Livre blanc détaillant les obligations légales et la protection des données.",
                    type = "document",
                    category = "Documents & Livres",
                    externalUrl = "https://www.cnil.fr/fr/reglement-europeen-protection-donnees",
                    thumbnailUrl = "https://images.unsplash.com/photo-1563986768609-322da13575f3?w=800&auto=format&fit=crop&q=80",
                    content = "Ce document synthétise les meilleures pratiques de chiffrement des données, gestion du consentement et politique de confidentialité conforme aux normes européennes.",
                    authorName = "Expertise Web",
                    status = "approved",
                    viewsCount = 98L,
                    likesCount = 16L,
                    commentsCount = 2L,
                    tags = listOf("rgpd", "securite", "document", "juridique")
                )
            )

            for (sample in samples) {
                repository.createPublication(sample)
            }
        }
    } catch (_: Exception) {
        // Non-blocking initial seed
    }
}

package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AdSenseBannerPlaceholder
import com.example.ui.theme.BrightAmberAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalAndAdScreen(
    initialSection: String = "privacy",
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val sections = listOf(
        "privacy" to "Confidentialité",
        "terms" to "Conditions d'utilisation",
        "adsense" to "Google AdSense",
        "about" to "À Propos & Contact"
    )

    var selectedTabIndex by remember {
        mutableIntStateOf(
            when (initialSection) {
                "terms" -> 1
                "adsense" -> 2
                "about" -> 3
                else -> 0
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Informations légales & Monétisation") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
        )

        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            sections.forEachIndexed { index, pair ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(pair.second, fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            when (selectedTabIndex) {
                0 -> {
                    // Privacy Policy
                    LegalSectionHeader(title = "Politique de Confidentialité (RGPD)")
                    LegalParagraph(
                        "Dernière mise à jour : 10 octobre 2026\n\n" +
                        "Bright Video accorde une importance primordiale à la protection de la vie privée de ses utilisateurs. Cette politique détaille la façon dont vos données sont collectées, stockées et protégées."
                    )
                    LegalSubheading("1. Collecte des données")
                    LegalParagraph(
                        "Lorsque vous vous connectez via Google Sign-In, nous recueillons votre nom d'affichage, votre adresse e-mail et votre photo de profil publique. Aucune information confidentielle de mot de passe n'est stockée sur nos serveurs."
                    )
                    LegalSubheading("2. Stockage et hébergement")
                    LegalParagraph(
                        "Les données de profil, métadonnées de publications, commentaires et mentions J'aime sont stockées de façon sécurisée via Cloud Firestore. Conformément à notre politique d'architecture, aucun fichier vidéo volumineux n'est hébergé directement dans notre base de données : seules les URL externes autorisées sont répertoriées."
                    )
                    LegalSubheading("3. Droits des utilisateurs")
                    LegalParagraph(
                        "Conformément au Règlement Général sur la Protection des Données (RGPD), vous disposez d'un droit d'accès, de rectification et de suppression de vos données personnelles. Vous pouvez demander la suppression de votre compte ou de vos contenus à tout moment."
                    )
                }
                1 -> {
                    // Terms of Service
                    LegalSectionHeader(title = "Conditions Générales d'Utilisation (CGU)")
                    LegalParagraph(
                        "En accédant à Bright Video, vous acceptez de respecter sans réserve les présentes conditions d'utilisation."
                    )
                    LegalSubheading("1. Rôle de la plateforme")
                    LegalParagraph(
                        "Bright Video est un espace communautaire de partage et de découverte de ressources externes (vidéos, tutoriels, articles, musiques et documents). Les utilisateurs partagent des hyperliens vers des plateformes tierces autorisées (YouTube, Vimeo, GitHub, etc.)."
                    )
                    LegalSubheading("2. Modération des contenus")
                    LegalParagraph(
                        "Toutes les publications soumises par les abonnés sont assujetties à une modération préalable par l'administrateur. L'administrateur se réserve le droit d'approuver, de refuser, de modifier ou de supprimer tout contenu non conforme, injurieux, illégal ou violant des droits d'auteur."
                    )
                    LegalSubheading("3. Propriété intellectuelle")
                    LegalParagraph(
                        "Les utilisateurs s'engagent à ne publier que des liens vers des contenus dont ils possèdent les droits ou qui sont légalement diffusables publiquement."
                    )
                }
                2 -> {
                    // AdSense Policy & Preparation
                    LegalSectionHeader(title = "Préparation Google AdSense & Monétisation")
                    AdSenseBannerPlaceholder()
                    Spacer(modifier = Modifier.height(14.dp))
                    LegalParagraph(
                        "Bright Video est conçu pour respecter scrupuleusement les règles du programme Google AdSense. Aucun clic forcé, incitation artificielle ou publicité mensongère n'est toléré."
                    )
                    LegalSubheading("Points de conformité AdSense intégrés :")
                    LegalParagraph(
                        "• Contenu original à haute valeur ajoutée rédigé et organisé en rubriques claires.\n" +
                        "• Présence intégrale des pages requises : Confidentialité, CGU, À Propos et Contact.\n" +
                        "• Espaces d'annonces clairement identifiés avec le libellé « Annonce » pour éviter toute confusion avec le contenu éditorial.\n" +
                        "• Respect de la navigation fluide et de la vitesse de chargement sur tous les appareils."
                    )
                    LegalSubheading("Note légale importante :")
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "Avertissement : L'approbation finale d'un compte ou d'un site par Google AdSense dépend exclusivement des critères d'évaluation de Google. L'application Bright Video fournit l'infrastructure conforme nécessaire mais ne peut garantir l'approbation commerciale par Google.",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
                3 -> {
                    // About & Contact
                    LegalSectionHeader(title = "À Propos de Bright Video & Contact")
                    LegalParagraph(
                        "Bright Video est une plateforme numérique innovante pensée pour connecter les créateurs de contenus et leur audience dans un environnement propre, modéré et performant."
                    )
                    LegalSubheading("Technologies employées :")
                    LegalParagraph(
                        "• Interface moderne Jetpack Compose Material 3 pour Android, tablettes et ordinateurs (ChromeOS)\n" +
                        "• Authentification sécurisée Google Sign-In via Credential Manager\n" +
                        "• Base de données temps réel Cloud Firestore avec règles de sécurité strictes\n" +
                        "• Hébergement performant sur Cloudflare Pages et Firebase Spark"
                    )
                    LegalSubheading("Contact de l'administrateur :")
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "bossokomlasimon0@gmail.com",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Pour toute question relative aux contenus, demandes de retrait ou partenariats, contactez l'administrateur par email.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("mailto:bossokomlasimon0@gmail.com")
                                        putExtra(Intent.EXTRA_SUBJECT, "[Bright Video] Demande de contact")
                                    }
                                    context.startActivity(intent)
                                }
                            ) {
                                Text("Envoyer un message")
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun LegalSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
        color = MaterialTheme.colorScheme.onBackground
    )
    Spacer(modifier = Modifier.height(10.dp))
}

@Composable
private fun LegalSubheading(text: String) {
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        color = BrightAmberAccent
    )
    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
private fun LegalParagraph(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

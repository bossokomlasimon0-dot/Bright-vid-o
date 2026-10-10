package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Publication
import com.example.data.model.PublicationType
import com.example.data.repository.PublicationRepository
import com.example.ui.components.AppCategories
import com.example.ui.theme.BrightAmberAccent
import com.example.ui.theme.BrightVioletPrimary
import com.example.ui.theme.StatusApprovedGreen
import com.example.ui.theme.StatusPendingYellow
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishScreen(
    publicationRepository: PublicationRepository,
    onPublishSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val isAdmin = publicationRepository.isUserAdmin()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(PublicationType.VIDEO) }
    var selectedCategory by remember { mutableStateOf(AppCategories.getOrElse(1) { "Technologie" }) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    var externalUrl by remember { mutableStateOf("") }
    var thumbnailUrl by remember { mutableStateOf("") }
    var contentText by remember { mutableStateOf("") }
    var tagsInput by remember { mutableStateOf("") }

    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmittedSuccess by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Publier un contenu",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Partagez des liens de vidéos, musiques, tutoriels, articles ou documents",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Community Moderation Notice
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            ),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (isAdmin) "Mode Administrateur : Publication directe" else "Système de modération actif",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isAdmin) {
                            "En tant qu'administrateur, votre contenu sera publié directement sans passer par la file d'attente."
                        } else {
                            "Pour garantir la qualité et la sécurité de Bright Video, les publications des abonnés sont vérifiées par notre équipe avant d'être visibles publiquement. Aucun fichier vidéo n'est hébergé dans Firebase : utilisez des liens externes légaux."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Content Type Selection Chips
        Text(
            text = "Type de publication",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PublicationType.entries.take(4).forEach { type ->
                FilterChip(
                    selected = selectedType == type,
                    onClick = { selectedType = type },
                    label = { Text(type.label, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PublicationType.entries.drop(4).forEach { type ->
                FilterChip(
                    selected = selectedType == type,
                    onClick = { selectedType = type },
                    label = { Text(type.label, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Title
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Titre de la publication *") },
            placeholder = { Text("Ex: Tutoriel complet Jetpack Compose ou Clip Vidéo officiel") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("publish_title_input"),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Category Dropdown
        ExposedDropdownMenuBox(
            expanded = categoryDropdownExpanded,
            onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedCategory,
                onValueChange = {},
                readOnly = true,
                label = { Text("Catégorie thématique *") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(
                expanded = categoryDropdownExpanded,
                onDismissRequest = { categoryDropdownExpanded = false }
            ) {
                AppCategories.filter { it != "Tous" }.forEach { cat ->
                    DropdownMenuItem(
                        text = { Text(cat) },
                        onClick = {
                            selectedCategory = cat
                            categoryDropdownExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // External URL (Strictly enforced)
        OutlinedTextField(
            value = externalUrl,
            onValueChange = { externalUrl = it },
            label = { Text("Lien externe vers le contenu (URL) *") },
            placeholder = {
                Text(
                    when (selectedType) {
                        PublicationType.VIDEO -> "https://www.youtube.com/watch?v=... ou Vimeo"
                        PublicationType.DOWNLOAD -> "https://github.com/... ou lien direct sécurisé"
                        PublicationType.MUSIC -> "https://soundcloud.com/... ou lien audio"
                        PublicationType.DOCUMENT -> "https://drive.google.com/... ou PDF web"
                        else -> "https://..."
                    }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("publish_url_input"),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Thumbnail URL (Optional)
        OutlinedTextField(
            value = thumbnailUrl,
            onValueChange = { thumbnailUrl = it },
            label = { Text("URL de la miniature / image (optionnel)") },
            placeholder = { Text("https://example.com/image.jpg") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Short Description
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description courte (optionnel)") },
            placeholder = { Text("Présentation en 1 à 2 phrases du contenu...") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Full Content (For articles and tutorials)
        if (selectedType == PublicationType.TUTORIAL || selectedType == PublicationType.ARTICLE) {
            OutlinedTextField(
                value = contentText,
                onValueChange = { contentText = it },
                label = { Text("Corps de l'article ou étapes du tutoriel") },
                placeholder = { Text("Rédigez ici le détail de votre article ou tutoriel pas à pas...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Tags
        OutlinedTextField(
            value = tagsInput,
            onValueChange = { tagsInput = it },
            label = { Text("Mots-clés / Tags (séparés par des virgules)") },
            placeholder = { Text("android, tutoriel, musique, gratuit") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (errorMessage != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }

        if (isSubmittedSuccess) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = StatusApprovedGreen.copy(alpha = 0.2f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = StatusApprovedGreen
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isAdmin) "Publication validée et en ligne !" else "Contenu soumis ! Il est désormais en attente de modération par l'administrateur.",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Submit Button
        Button(
            onClick = {
                if (title.trim().length < 3) {
                    errorMessage = "Veuillez saisir un titre d'au moins 3 caractères"
                    return@Button
                }
                if (!externalUrl.trim().startsWith("http://") && !externalUrl.trim().startsWith("https://")) {
                    errorMessage = "Veuillez saisir une URL valide commençant par http:// ou https://"
                    return@Button
                }

                isSubmitting = true
                errorMessage = null

                val tags = tagsInput.split(",")
                    .map { it.trim().lowercase() }
                    .filter { it.isNotBlank() }
                    .take(8)

                val pub = Publication(
                    title = title.trim(),
                    description = description.trim(),
                    type = selectedType.id,
                    category = selectedCategory,
                    externalUrl = externalUrl.trim(),
                    thumbnailUrl = thumbnailUrl.trim(),
                    content = contentText.trim(),
                    authorId = currentUser?.uid ?: "",
                    authorName = currentUser?.displayName ?: "Abonné",
                    authorAvatarUrl = currentUser?.photoUrl?.toString() ?: "",
                    tags = tags
                )

                scope.launch {
                    val result = publicationRepository.createPublication(pub)
                    isSubmitting = false
                    if (result.isSuccess) {
                        isSubmittedSuccess = true
                        Toast.makeText(
                            context,
                            if (isAdmin) "Publication approuvée et publiée !" else "Publication soumise en attente de validation",
                            Toast.LENGTH_LONG
                        ).show()
                        onPublishSuccess()
                    } else {
                        errorMessage = "Erreur lors de la publication: ${result.exceptionOrNull()?.message}"
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("submit_publication_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = BrightVioletPrimary
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = !isSubmitting
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Enregistrement en cours...")
            } else {
                Text(
                    text = if (isAdmin) "Publier immédiatement (Admin)" else "Soumettre pour modération",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

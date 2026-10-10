package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Publication
import com.example.data.repository.PublicationRepository
import com.example.ui.components.AdSenseBannerPlaceholder
import com.example.ui.components.CategoryChipRow
import com.example.ui.components.PublicationCard
import com.example.ui.theme.BrightAmberAccent
import com.example.ui.theme.BrightCyan
import com.example.ui.theme.BrightVioletPrimary
import com.example.ui.theme.BrightVioletPrimaryDark

private val FilterTabs = listOf(
    "all" to ("Tous" to Icons.Default.AllInclusive),
    "video" to ("Vidéos" to Icons.Default.PlayArrow),
    "tutorial" to ("Tutos" to Icons.Default.School),
    "article" to ("Articles" to Icons.Default.Article),
    "music" to ("Musique" to Icons.Default.Headphones),
    "download" to ("Fichiers" to Icons.Default.Download)
)

@Composable
fun HomeScreen(
    publicationRepository: PublicationRepository,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToPublish: () -> Unit,
    onNavigateToExplore: () -> Unit,
    onNavigateToLegal: (String) -> Unit,
    isAdmin: Boolean = false,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf("Tous") }
    var selectedTypeIndex by remember { mutableIntStateOf(0) }

    val selectedTypeId = FilterTabs[selectedTypeIndex].first

    val publicationsFlow = remember(selectedCategory, selectedTypeId) {
        publicationRepository.observeApprovedPublications(
            category = selectedCategory,
            type = selectedTypeId
        )
    }
    val publications by publicationsFlow.collectAsState(initial = emptyList())

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Hero Community Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        BrightVioletPrimaryDark,
                                        MaterialTheme.colorScheme.surfaceVariant
                                    )
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = BrightAmberAccent
                            ) {
                                Text(
                                    text = "COMMUNAUTÉ BRIGHT VIDEO",
                                    color = Color.Black,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Partagez et explorez les meilleures ressources",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                ),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Vidéos externes, tutoriels, articles, téléchargements et musiques sans aucun stockage direct dans Firebase.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Row {
                                Button(
                                    onClick = onNavigateToPublish,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = BrightAmberAccent,
                                        contentColor = Color.Black
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("hero_publish_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Publier un contenu", fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Button(
                                    onClick = onNavigateToExplore,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White.copy(alpha = 0.2f),
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Rechercher")
                                }
                            }
                        }
                    }
                }
            }

            // Categories Filter Chips
            item {
                Text(
                    text = "Catégories",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                CategoryChipRow(
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it }
                )
            }

            // Type Filter Tabs
            item {
                ScrollableTabRow(
                    selectedTabIndex = selectedTypeIndex,
                    edgePadding = 16.dp,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterTabs.forEachIndexed { index, (typeKey, pair) ->
                        val (label, icon) = pair
                        Tab(
                            selected = selectedTypeIndex == index,
                            onClick = { selectedTypeIndex = index },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(label)
                                }
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Google AdSense Compliant Banner Slot
            item {
                AdSenseBannerPlaceholder(
                    onOpenLegal = { onNavigateToLegal("privacy") }
                )
            }

            // Feed Section Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Publications récentes",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${publications.size} disponible${if (publications.size > 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Publication Items
            if (publications.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.AllInclusive,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Aucune publication pour le moment",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Soyez le premier à partager une vidéo externe, un tutoriel ou un document avec la communauté Bright Video !",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onNavigateToPublish,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text("Publier maintenant")
                            }
                        }
                    }
                }
            } else {
                items(publications, key = { it.id }) { pub ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                        PublicationCard(
                            publication = pub,
                            onClick = { onNavigateToDetail(pub.id) }
                        )
                    }
                }
            }
        }

        // Quick Publish FAB
        FloatingActionButton(
            onClick = onNavigateToPublish,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("home_fab_publish"),
            containerColor = BrightAmberAccent,
            contentColor = Color.Black
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Publier")
        }
    }
}

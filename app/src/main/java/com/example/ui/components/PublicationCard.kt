package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.Publication
import com.example.data.model.PublicationType
import com.example.ui.theme.BrightAmberAccent
import com.example.ui.theme.BrightCyan
import com.example.ui.theme.BrightVioletPrimary
import com.example.ui.theme.StatusApprovedGreen
import com.example.ui.theme.StatusPendingYellow
import com.example.ui.theme.StatusRejectedRed

@Composable
fun PublicationCard(
    publication: Publication,
    onClick: () -> Unit,
    showStatusBadge: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("publication_card_${publication.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Thumbnail / Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                BrightVioletPrimary.copy(alpha = 0.8f),
                                MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    )
            ) {
                if (publication.thumbnailUrl.isNotBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(publication.thumbnailUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = publication.title,
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Placeholder based on type
                    Box(
                        modifier = Modifier.matchParentSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getTypeIcon(publication.type),
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(52.dp)
                        )
                    }
                }

                // Type Badge (top left)
                Surface(
                    modifier = Modifier
                        .padding(10.dp)
                        .align(Alignment.TopStart),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.7f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = getTypeIcon(publication.type),
                            contentDescription = null,
                            tint = BrightAmberAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = PublicationType.fromId(publication.type).label,
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                // Category Badge (top right)
                Surface(
                    modifier = Modifier
                        .padding(10.dp)
                        .align(Alignment.TopEnd),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = publication.category,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Moderation Status Badge if requested
                if (showStatusBadge) {
                    val (statusColor, statusText) = when (publication.status) {
                        "approved" -> StatusApprovedGreen to "Approuvé"
                        "rejected" -> StatusRejectedRed to "Refusé"
                        else -> StatusPendingYellow to "En attente"
                    }
                    Surface(
                        modifier = Modifier
                            .padding(10.dp)
                            .align(Alignment.BottomStart),
                        shape = RoundedCornerShape(6.dp),
                        color = statusColor
                    ) {
                        Text(
                            text = statusText,
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Body
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = publication.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (publication.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = publication.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Author row & Metrics
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Author
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(BrightVioletPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (publication.authorName.take(1).ifEmpty { "B" }).uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = BrightVioletPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = publication.authorName.ifEmpty { "Abonné" },
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Metrics (Views, Likes, Comments)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        MetricIconText(icon = Icons.Default.Visibility, value = publication.viewsCount.toString())
                        Spacer(modifier = Modifier.width(10.dp))
                        MetricIconText(icon = Icons.Default.Favorite, value = publication.likesCount.toString(), tint = BrightAmberAccent)
                        Spacer(modifier = Modifier.width(10.dp))
                        MetricIconText(icon = Icons.Default.ChatBubbleOutline, value = publication.commentsCount.toString())
                    }
                }
            }
        }
    }
}

@Composable
fun MetricIconText(
    icon: ImageVector,
    value: String,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            color = tint
        )
    }
}

fun getTypeIcon(type: String): ImageVector {
    return when (type) {
        "video" -> Icons.Default.PlayArrow
        "tutorial" -> Icons.Default.School
        "article" -> Icons.Default.MenuBook
        "music" -> Icons.Default.Headphones
        "download" -> Icons.Default.Download
        "document" -> Icons.Default.Description
        "photo" -> Icons.Default.PhotoCamera
        else -> Icons.Default.PlayArrow
    }
}

package com.example.ui.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.repository.PublicationRepository
import com.example.data.repository.UserRepository
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.DetailScreen
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LegalAndAdScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.PublishScreen
import com.example.ui.theme.BrightAmberAccent
import com.example.ui.theme.BrightVioletPrimary

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Home : BottomNavItem("home", "Accueil", Icons.Default.Home, Icons.Outlined.Home)
    data object Explore : BottomNavItem("explore", "Explorer", Icons.Default.Search, Icons.Outlined.Search)
    data object Publish : BottomNavItem("publish", "Publier", Icons.Default.AddCircle, Icons.Default.AddCircle)
    data object Admin : BottomNavItem("admin_dashboard", "Admin", Icons.Default.AdminPanelSettings, Icons.Default.AdminPanelSettings)
    data object Profile : BottomNavItem("profile", "Profil", Icons.Default.Person, Icons.Outlined.Person)
}

@Composable
fun AppNavigation(
    publicationRepository: PublicationRepository,
    userRepository: UserRepository,
    onSignOut: () -> Unit
) {
    val isAdmin = publicationRepository.isUserAdmin()

    val pendingFlow = remember(isAdmin) {
        if (isAdmin) publicationRepository.observePendingPublications()
        else kotlinx.coroutines.flow.flowOf(emptyList<com.example.data.model.Publication>())
    }
    val pendingCount by pendingFlow.collectAsState(initial = emptyList())

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

    val bottomNavItems = remember(isAdmin) {
        if (isAdmin) {
            listOf(
                BottomNavItem.Home,
                BottomNavItem.Explore,
                BottomNavItem.Publish,
                BottomNavItem.Admin,
                BottomNavItem.Profile
            )
        } else {
            listOf(
                BottomNavItem.Home,
                BottomNavItem.Explore,
                BottomNavItem.Publish,
                BottomNavItem.Profile
            )
        }
    }

    Scaffold(
        bottomBar = {
            if (currentScreen is Screen.Home ||
                currentScreen is Screen.Explore ||
                currentScreen is Screen.Publish ||
                currentScreen is Screen.AdminDashboard ||
                currentScreen is Screen.Profile
            ) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    val currentRoute = currentScreen.route
                    bottomNavItems.forEach { item ->
                        val isSelected = currentRoute == item.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                currentScreen = when (item) {
                                    BottomNavItem.Home -> Screen.Home
                                    BottomNavItem.Explore -> Screen.Explore
                                    BottomNavItem.Publish -> Screen.Publish
                                    BottomNavItem.Admin -> Screen.AdminDashboard
                                    BottomNavItem.Profile -> Screen.Profile
                                }
                            },
                            icon = {
                                if (item == BottomNavItem.Admin && pendingCount.isNotEmpty()) {
                                    BadgedBox(badge = {
                                        Badge(containerColor = BrightAmberAccent) {
                                            Text(pendingCount.size.toString(), color = androidx.compose.ui.graphics.Color.Black)
                                        }
                                    }) {
                                        Icon(
                                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = item.title
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.title
                                    )
                                }
                            },
                            label = { Text(item.title) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = BrightVioletPrimary,
                                selectedTextColor = BrightVioletPrimary,
                                indicatorColor = BrightVioletPrimary.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.testTag("nav_item_${item.route}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val screen = currentScreen) {
                is Screen.Home -> {
                    HomeScreen(
                        publicationRepository = publicationRepository,
                        onNavigateToDetail = { pubId -> currentScreen = Screen.Detail(pubId) },
                        onNavigateToPublish = { currentScreen = Screen.Publish },
                        onNavigateToExplore = { currentScreen = Screen.Explore },
                        onNavigateToLegal = { sec -> currentScreen = Screen.LegalInfo(sec) },
                        isAdmin = isAdmin
                    )
                }
                is Screen.Explore -> {
                    ExploreScreen(
                        publicationRepository = publicationRepository,
                        onNavigateToDetail = { pubId -> currentScreen = Screen.Detail(pubId) }
                    )
                }
                is Screen.Publish -> {
                    PublishScreen(
                        publicationRepository = publicationRepository,
                        onPublishSuccess = { currentScreen = Screen.Home }
                    )
                }
                is Screen.AdminDashboard -> {
                    AdminDashboardScreen(
                        publicationRepository = publicationRepository,
                        onBack = { currentScreen = Screen.Home },
                        onNavigateToDetail = { pubId -> currentScreen = Screen.Detail(pubId) }
                    )
                }
                is Screen.Profile -> {
                    ProfileScreen(
                        userRepository = userRepository,
                        publicationRepository = publicationRepository,
                        onSignOut = onSignOut,
                        onNavigateToDetail = { pubId -> currentScreen = Screen.Detail(pubId) },
                        onNavigateToAdmin = { currentScreen = Screen.AdminDashboard },
                        onNavigateToLegal = { sec -> currentScreen = Screen.LegalInfo(sec) }
                    )
                }
                is Screen.Detail -> {
                    DetailScreen(
                        publicationId = screen.publicationId,
                        publicationRepository = publicationRepository,
                        userRepository = userRepository,
                        onBack = { currentScreen = Screen.Home },
                        onNavigateToLegal = { sec -> currentScreen = Screen.LegalInfo(sec) }
                    )
                }
                is Screen.LegalInfo -> {
                    LegalAndAdScreen(
                        initialSection = screen.section,
                        onBack = { currentScreen = Screen.Home }
                    )
                }
            }
        }
    }
}

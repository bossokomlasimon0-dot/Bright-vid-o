package com.example.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Explore : Screen("explore")
    data object Publish : Screen("publish")
    data object AdminDashboard : Screen("admin_dashboard")
    data object Profile : Screen("profile")
    data class Detail(val publicationId: String) : Screen("detail/$publicationId")
    data class LegalInfo(val section: String = "privacy") : Screen("legal/$section")
}

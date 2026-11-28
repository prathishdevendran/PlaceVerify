package com.example.placeverify.ui.student

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.placeverify.data.ApplicationItem
import com.example.placeverify.data.NotificationItem
import com.example.placeverify.data.PlacementOpportunity
import com.example.placeverify.data.repository.PlaceVerifyRepository
import com.example.placeverify.ui.theme.DeepNavy

sealed class StudentTab(val route: String, val title: String, val icon: ImageVector) {
    object Home : StudentTab("home", "Drives", Icons.Default.Home)
    object Saved : StudentTab("saved", "Saved", Icons.Default.Bookmark)
    object Applications : StudentTab("applications", "Applications", Icons.AutoMirrored.Filled.Assignment)
    object Notifications : StudentTab("notifications", "Alerts", Icons.Default.Notifications)
    object Interview : StudentTab("interview", "Interview", Icons.Default.School)
}

@Composable
fun StudentMainScreen(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToProfile: () -> Unit
) {
    var selectedTab by remember { mutableStateOf<StudentTab>(StudentTab.Home) }
    val tabs = listOf(
        StudentTab.Home,
        StudentTab.Saved,
        StudentTab.Applications,
        StudentTab.Interview,
        StudentTab.Notifications
    )
    val repository = remember { PlaceVerifyRepository() }

    var opportunities by remember { mutableStateOf<List<PlacementOpportunity>>(emptyList()) }
    var applications by remember { mutableStateOf<List<ApplicationItem>>(emptyList()) }
    var notifications by remember { mutableStateOf<List<NotificationItem>>(emptyList()) }

    // Preload user profile into memory cache immediately on portal load
    LaunchedEffect(Unit) {
        repository.getCurrentUserProfile()
    }

    LaunchedEffect(selectedTab) {
        val savedIds = try { repository.savedPlacementIds() } catch (_: Exception) { emptySet() }
        opportunities = repository.getOpportunities().map { it.copy(isSaved = it.id in savedIds) }
        applications = repository.getApplications()
        notifications = repository.getNotifications()
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = DeepNavy
            ) {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title) },
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DeepNavy,
                            selectedTextColor = DeepNavy,
                            indicatorColor = DeepNavy.copy(alpha = 0.12f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                is StudentTab.Home -> StudentHomeScreen(
                    opportunities = opportunities,
                    onOpportunityClick = { opp -> onNavigateToDetail(opp.id) },
                    onProfileClick = onNavigateToProfile
                )
                is StudentTab.Saved -> SavedOpportunitiesScreen(
                    opportunities = opportunities.filter { it.isSaved },
                    onOpportunityClick = { opp -> onNavigateToDetail(opp.id) }
                )
                is StudentTab.Applications -> ApplicationTrackerScreen(
                    applications = applications
                )
                is StudentTab.Notifications -> StudentNotificationsScreen(
                    notifications = notifications
                )
                is StudentTab.Interview -> InterviewPreparationScreen(repository)
            }
        }
    }
}

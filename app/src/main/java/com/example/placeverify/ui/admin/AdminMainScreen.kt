package com.example.placeverify.ui.admin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.GppBad
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.placeverify.data.MisinformationReport
import com.example.placeverify.data.VerificationRequest
import com.example.placeverify.data.repository.PlaceVerifyRepository
import com.example.placeverify.ui.theme.DeepNavy

sealed class AdminTab(val route: String, val title: String, val icon: ImageVector) {
    object Analytics : AdminTab("analytics", "Analytics", Icons.Default.Analytics)
    object Verifications : AdminTab("verifications", "Verifications", Icons.Default.VerifiedUser)
    object Users : AdminTab("users", "Users", Icons.Default.People)
    object Misinformation : AdminTab("misinformation", "Scam Shield", Icons.Default.GppBad)
}

@Composable
fun AdminMainScreen(
    onNavigateToProfile: () -> Unit
) {
    var selectedTab by remember { mutableStateOf<AdminTab>(AdminTab.Analytics) }
    val tabs = listOf(
        AdminTab.Analytics,
        AdminTab.Verifications,
        AdminTab.Users,
        AdminTab.Misinformation
    )
    val repository = remember { PlaceVerifyRepository() }

    var verificationRequests by remember { mutableStateOf<List<VerificationRequest>>(emptyList()) }
    var reports by remember { mutableStateOf<List<MisinformationReport>>(emptyList()) }

    LaunchedEffect(selectedTab) {
        verificationRequests = repository.getVerificationRequests()
        reports = repository.getMisinformationReports()
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
        Box(modifier = androidx.compose.ui.Modifier.padding(innerPadding)) {
            when (selectedTab) {
                is AdminTab.Analytics -> AdminDashboardScreen(onProfileClick = onNavigateToProfile)
                is AdminTab.Verifications -> AdminVerificationScreen(requests = verificationRequests)
                is AdminTab.Users -> AdminUserManagementScreen()
                is AdminTab.Misinformation -> AdminMisinformationScreen(reports = reports)
            }
        }
    }
}

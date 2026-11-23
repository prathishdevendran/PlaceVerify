package com.example.placeverify.ui.officer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.placeverify.data.MockData
import com.example.placeverify.ui.theme.DeepNavy

sealed class OfficerTab(val route: String, val title: String, val icon: ImageVector) {
    object Drives : OfficerTab("drives", "Drives", Icons.Default.Work)
    object Post : OfficerTab("post", "Post Drive", Icons.Default.AddCircle)
    object Reports : OfficerTab("reports", "Reports", Icons.Default.Report)
    object Eligibility : OfficerTab("eligibility", "Students", Icons.Default.People)
}

@Composable
fun OfficerMainScreen(
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf<OfficerTab>(OfficerTab.Drives) }
    val tabs = listOf(OfficerTab.Drives, OfficerTab.Post, OfficerTab.Reports, OfficerTab.Eligibility)

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
                is OfficerTab.Drives -> OfficerDrivesScreen(opportunities = MockData.opportunities)
                is OfficerTab.Post -> CreateAnnouncementScreen(onPostSuccess = { selectedTab = OfficerTab.Drives })
                is OfficerTab.Reports -> OfficerReportsScreen(reports = MockData.misinformationReports)
                is OfficerTab.Eligibility -> StudentEligibilityScreen()
            }
        }
    }
}

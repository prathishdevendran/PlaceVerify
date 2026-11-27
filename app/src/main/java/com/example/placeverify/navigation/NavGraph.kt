package com.example.placeverify.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.placeverify.data.PlacementOpportunity
import com.example.placeverify.data.UserRole
import com.example.placeverify.data.repository.PlaceVerifyRepository
import com.example.placeverify.ui.admin.AdminMainScreen
import com.example.placeverify.ui.auth.LoginScreen
import com.example.placeverify.ui.auth.RegisterScreen
import com.example.placeverify.ui.auth.RoleSelectionScreen
import com.example.placeverify.ui.auth.SplashScreen
import com.example.placeverify.ui.officer.OfficerMainScreen
import com.example.placeverify.ui.profile.UserProfileScreen
import com.example.placeverify.ui.student.OpportunityDetailScreen
import com.example.placeverify.ui.student.ReportSuspiciousScreen
import com.example.placeverify.ui.student.StudentMainScreen

@Composable
fun PlaceVerifyNavGraph() {
    val navController = rememberNavController()
    val repository = remember { PlaceVerifyRepository() }

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(
                onNavigateToDashboard = { role ->
                    val destination = when (role.uppercase()) {
                        "PLACEMENT_OFFICER" -> "officer_main"
                        "SUPER_ADMIN" -> "admin_main"
                        else -> "student_main"
                    }
                    navController.navigate(destination) {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                onNavigateToRoleSelection = {
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        composable("login") {
            LoginScreen(
                onLoginSuccess = { userRole ->
                    val destination = when (userRole.uppercase()) {
                        "PLACEMENT_OFFICER" -> "officer_main"
                        "SUPER_ADMIN" -> "admin_main"
                        else -> "student_main"
                    }
                    navController.navigate(destination) {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegisterRoleSelection = {
                    navController.navigate("register_role_selection")
                }
            )
        }

        composable("register_role_selection") {
            RoleSelectionScreen(
                onRoleSelected = { role ->
                    navController.navigate("register/${role.name}")
                },
                onBackToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "register/{role}",
            arguments = listOf(navArgument("role") { type = NavType.StringType })
        ) { backStackEntry ->
            val roleStr = backStackEntry.arguments?.getString("role") ?: UserRole.STUDENT.name
            val role = try { UserRole.valueOf(roleStr) } catch (e: Exception) { UserRole.STUDENT }

            RegisterScreen(
                role = role,
                onRegisterSuccess = { regRole ->
                    val destination = when (regRole) {
                        UserRole.PLACEMENT_OFFICER -> "officer_main"
                        UserRole.SUPER_ADMIN -> "admin_main"
                        else -> "student_main"
                    }
                    navController.navigate(destination) {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onBackToLogin = { navController.navigate("login") { popUpTo("login") { inclusive = true } } }
            )
        }

        composable("student_main") {
            StudentMainScreen(
                onNavigateToDetail = { oppId ->
                    navController.navigate("opportunity_detail/$oppId")
                },
                onNavigateToProfile = {
                    navController.navigate("user_profile")
                }
            )
        }

        composable("officer_main") {
            OfficerMainScreen(
                onNavigateToProfile = {
                    navController.navigate("user_profile")
                }
            )
        }

        composable("admin_main") {
            AdminMainScreen(
                onNavigateToProfile = {
                    navController.navigate("user_profile")
                }
            )
        }

        composable("user_profile") {
            UserProfileScreen(
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
                onReportSuspicious = {
                    navController.navigate("report_suspicious")
                }
            )
        }

        composable(
            route = "opportunity_detail/{opportunityId}",
            arguments = listOf(navArgument("opportunityId") { type = NavType.StringType })
        ) { backStackEntry ->
            val opportunityId = backStackEntry.arguments?.getString("opportunityId") ?: ""
            var opps by remember { mutableStateOf<List<PlacementOpportunity>>(emptyList()) }
            LaunchedEffect(opportunityId) {
                opps = repository.getOpportunities()
            }
            OpportunityDetailScreen(
                opportunityId = opportunityId,
                opportunities = opps,
                onBack = { navController.popBackStack() },
                onApply = { oppId -> }
            )
        }

        composable("report_suspicious") {
            ReportSuspiciousScreen(
                onBack = { navController.popBackStack() },
                onSubmitSuccess = {
                    navController.popBackStack()
                }
            )
        }
    }
}

package com.example.placeverify.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.placeverify.data.MockData
import com.example.placeverify.data.UserRole
import com.example.placeverify.ui.admin.AdminMainScreen
import com.example.placeverify.ui.auth.LoginScreen
import com.example.placeverify.ui.auth.RegisterScreen
import com.example.placeverify.ui.auth.RoleSelectionScreen
import com.example.placeverify.ui.auth.SplashScreen
import com.example.placeverify.ui.officer.OfficerMainScreen
import com.example.placeverify.ui.student.OpportunityDetailScreen
import com.example.placeverify.ui.student.ReportSuspiciousScreen
import com.example.placeverify.ui.student.StudentMainScreen

@Composable
fun PlaceVerifyNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(
                onNavigateToRoleSelection = {
                    navController.navigate("role_selection") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        composable("role_selection") {
            RoleSelectionScreen(
                onRoleSelected = { role ->
                    navController.navigate("login/${role.name}")
                }
            )
        }

        composable(
            route = "login/{role}",
            arguments = listOf(navArgument("role") { type = NavType.StringType })
        ) { backStackEntry ->
            val roleStr = backStackEntry.arguments?.getString("role") ?: UserRole.STUDENT.name
            val role = try { UserRole.valueOf(roleStr) } catch (e: Exception) { UserRole.STUDENT }

            LoginScreen(
                role = role,
                onLoginSuccess = { selectedRole ->
                    when (selectedRole) {
                        UserRole.STUDENT -> navController.navigate("student_main") { popUpTo("role_selection") { inclusive = true } }
                        UserRole.PLACEMENT_OFFICER -> navController.navigate("officer_main") { popUpTo("role_selection") { inclusive = true } }
                        UserRole.SUPER_ADMIN -> navController.navigate("admin_main") { popUpTo("role_selection") { inclusive = true } }
                    }
                },
                onNavigateToRegister = { regRole ->
                    navController.navigate("register/${regRole.name}")
                },
                onBack = { navController.popBackStack() }
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
                    when (regRole) {
                        UserRole.STUDENT -> navController.navigate("student_main") { popUpTo("role_selection") { inclusive = true } }
                        UserRole.PLACEMENT_OFFICER -> navController.navigate("officer_main") { popUpTo("role_selection") { inclusive = true } }
                        UserRole.SUPER_ADMIN -> navController.navigate("admin_main") { popUpTo("role_selection") { inclusive = true } }
                    }
                },
                onBackToLogin = { navController.popBackStack() }
            )
        }

        composable("student_main") {
            StudentMainScreen(
                onNavigateToDetail = { oppId ->
                    navController.navigate("opportunity_detail/$oppId")
                },
                onReportSuspicious = {
                    navController.navigate("report_suspicious")
                },
                onLogout = {
                    navController.navigate("role_selection") { popUpTo("student_main") { inclusive = true } }
                }
            )
        }

        composable("officer_main") {
            OfficerMainScreen(
                onLogout = {
                    navController.navigate("role_selection") { popUpTo("officer_main") { inclusive = true } }
                }
            )
        }

        composable("admin_main") {
            AdminMainScreen(
                onLogout = {
                    navController.navigate("role_selection") { popUpTo("admin_main") { inclusive = true } }
                }
            )
        }

        composable(
            route = "opportunity_detail/{opportunityId}",
            arguments = listOf(navArgument("opportunityId") { type = NavType.StringType })
        ) { backStackEntry ->
            val opportunityId = backStackEntry.arguments?.getString("opportunityId") ?: "1"
            OpportunityDetailScreen(
                opportunityId = opportunityId,
                opportunities = MockData.opportunities,
                onBack = { navController.popBackStack() },
                onApply = { oppId ->
                    // Handle apply action in mock data if needed
                }
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

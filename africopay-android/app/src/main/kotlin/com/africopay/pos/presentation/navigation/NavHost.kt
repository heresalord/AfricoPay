package com.africopay.pos.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.africopay.pos.presentation.dashboard.DashboardScreen
import com.africopay.pos.presentation.diagnostics.DiagnosticsScreen
import com.africopay.pos.presentation.history.HistoryScreen
import com.africopay.pos.presentation.home.HomeScreen
import com.africopay.pos.presentation.payment.PaymentMethodsScreen
import com.africopay.pos.presentation.payment.ProcessingScreen
import com.africopay.pos.presentation.receipt.ReceiptScreen
import com.africopay.pos.presentation.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Home          : Screen("home")
    object PaymentMethods: Screen("payment_methods/{amountCents}") {
        fun createRoute(amountCents: Long) = "payment_methods/$amountCents"
    }
    object Processing    : Screen("processing/{amountCents}/{method}") {
        fun createRoute(amountCents: Long, method: String) = "processing/$amountCents/$method"
    }
    object Receipt       : Screen("receipt/{transactionId}") {
        fun createRoute(transactionId: String) = "receipt/$transactionId"
    }
    object History       : Screen("history")
    object Dashboard     : Screen("dashboard")
    object Diagnostics   : Screen("diagnostics")
    object Settings      : Screen("settings")
}

@Composable
fun AfricoPayNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Home.route) {

        composable(Screen.Home.route) {
            HomeScreen(
                onContinue = { amountCents ->
                    navController.navigate(Screen.PaymentMethods.createRoute(amountCents))
                },
                onHistoryClick  = { navController.navigate(Screen.History.route) },
                onDashboardClick = { navController.navigate(Screen.Dashboard.route) },
                onDiagnosticsClick = { navController.navigate(Screen.Diagnostics.route) },
                onSettingsClick = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(
            route = Screen.PaymentMethods.route,
            arguments = listOf(navArgument("amountCents") { type = NavType.LongType })
        ) { backStackEntry ->
            val amountCents = backStackEntry.arguments?.getLong("amountCents") ?: 0L
            PaymentMethodsScreen(
                amountCents = amountCents,
                onMethodSelected = { method ->
                    navController.navigate(Screen.Processing.createRoute(amountCents, method))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Processing.route,
            arguments = listOf(
                navArgument("amountCents") { type = NavType.LongType },
                navArgument("method") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val amountCents = backStackEntry.arguments?.getLong("amountCents") ?: 0L
            val method = backStackEntry.arguments?.getString("method") ?: ""
            ProcessingScreen(
                amountCents = amountCents,
                paymentMethodKey = method,
                onTransactionComplete = { transactionId ->
                    navController.navigate(Screen.Receipt.createRoute(transactionId)) {
                        popUpTo(Screen.Home.route)
                    }
                },
                onCancel = { navController.popBackStack(Screen.Home.route, inclusive = false) }
            )
        }

        composable(
            route = Screen.Receipt.route,
            arguments = listOf(navArgument("transactionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getString("transactionId") ?: ""
            ReceiptScreen(
                transactionId = transactionId,
                onDone = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Home.route) } },
                onViewHistory = { navController.navigate(Screen.History.route) }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Diagnostics.route) {
            DiagnosticsScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}

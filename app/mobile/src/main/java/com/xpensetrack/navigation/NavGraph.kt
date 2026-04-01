package com.xpensetrack.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.xpensetrack.ui.screens.*

object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val MAIN = "main"
    const val ADD_EXPENSE = "add_expense"
    const val REPORTS = "reports"
    const val FRIENDS = "friends"
    const val ADD_FRIENDS = "add_friends"
    const val DRAGON = "dragon"
    const val SHOP = "shop"
    const val PIGGY_BANK = "piggy_bank"
    const val CHAT = "chat"
    const val NOTIFICATIONS = "notifications"
    const val PAYMENT = "payment"
    const val EDIT_PROFILE = "edit_profile"
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Routes.SPLASH) {
        composable(Routes.SPLASH) { SplashScreen(navController) }
        composable(Routes.ONBOARDING) { OnboardingScreen(navController) }
        composable(Routes.LOGIN) { LoginScreen(navController) }
        composable(Routes.SIGNUP) { SignupScreen(navController) }
        composable(Routes.MAIN) { MainScreen(navController) }
        composable(Routes.ADD_EXPENSE) { AddExpenseScreen(navController) }
        composable(Routes.REPORTS) { ReportsScreen(navController) }
        composable(Routes.FRIENDS) { FriendsScreen(navController) }
        composable(Routes.ADD_FRIENDS) { AddFriendsScreen(navController) }
        composable(Routes.DRAGON) { DragonScreen(navController) }
        composable(Routes.SHOP) { ShopScreen(navController) }
        composable(Routes.PIGGY_BANK) { PiggyBankScreen(navController) }
        composable(Routes.CHAT) { ChatScreen(navController) }
        composable(Routes.NOTIFICATIONS) { NotificationsScreen(navController) }
        composable(Routes.PAYMENT) { PaymentScreen(navController) }
        composable(Routes.EDIT_PROFILE) { EditProfileScreen(navController) }
    }
}

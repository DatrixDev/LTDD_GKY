package com.example.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.ui.screen.AddUserScreen
import com.example.myapplication.ui.screen.EditUserScreen
import com.example.myapplication.ui.screen.LoginScreen
import com.example.myapplication.ui.screen.NoPermissionScreen
import com.example.myapplication.ui.screen.UserListScreen

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }

        composable(Screen.UserList.route) {
            UserListScreen(navController = navController)
        }

        composable(Screen.AddUser.route) {
            AddUserScreen(navController = navController)
        }
        composable(
            route = Screen.NoPermission.route,
            arguments = listOf(
                navArgument("username") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: ""
            NoPermissionScreen(
                navController = navController,
                username = username
            )
        }
        composable(
            route = Screen.EditUser.route,
            arguments = listOf(
                navArgument("username") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: ""
            EditUserScreen(
                navController = navController,
                username = username
            )
        }
    }
}
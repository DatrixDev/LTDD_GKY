package com.example.myapplication.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object UserList : Screen("user_list")
    object AddUser : Screen("add_user")
    object NoPermission : Screen("no_permission")
    object EditUser : Screen("edit_user/{username}") {
        fun createRoute(username: String): String = "edit_user/$username"
    }
}
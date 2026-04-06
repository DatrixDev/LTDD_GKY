package com.example.myapplication.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.myapplication.firebase.FirebaseConfig
import com.example.myapplication.model.UserModel
import com.example.myapplication.navigation.Screen
import com.example.myapplication.ui.components.UserItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserListScreen(navController: NavController) {
    var userList by remember { mutableStateOf(listOf<UserModel>()) }
    val context = LocalContext.current

    fun loadUsers() {
        FirebaseConfig.firestore.collection("users")
            .get()
            .addOnSuccessListener { result ->
                userList = result.documents.map { doc ->
                    UserModel(
                        username = doc.getString("username") ?: "",
                        password = doc.getString("password") ?: "",
                        role = doc.getString("role") ?: "",
                        image = doc.getString("image") ?: ""
                    )
                }
            }
    }

    LaunchedEffect(Unit) {
        loadUsers()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Danh sách User",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.UserList.route) { inclusive = true }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Đăng xuất",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddUser.route) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Thêm user"
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(top = 8.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(userList) { user ->
                UserItem(
                    user = user,
                    onEditClick = {
                        navController.navigate(Screen.EditUser.createRoute(user.username))
                    },
                    onDeleteClick = {
                        FirebaseConfig.firestore.collection("users")
                            .whereEqualTo("username", user.username)
                            .get()
                            .addOnSuccessListener { result ->
                                result.documents.forEach { it.reference.delete() }
                                Toast.makeText(context, "Đã xóa", Toast.LENGTH_SHORT).show()
                                loadUsers()
                            }
                    }
                )
            }
        }
    }
}
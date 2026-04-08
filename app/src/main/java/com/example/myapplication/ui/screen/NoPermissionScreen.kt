package com.example.myapplication.ui.screen

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.myapplication.firebase.FirebaseConfig
import com.example.myapplication.navigation.Screen
import com.example.myapplication.utils.PasswordUtils

data class UserItem(
    val username: String = "",
    val role: String = "",
    val image: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoPermissionScreen(
    navController: NavController,
    username: String
) {
    val context = LocalContext.current

    var newUsername by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var image by remember { mutableStateOf("") }
    var newImageUri by remember { mutableStateOf<Uri?>(null) }

    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var isFetching by remember { mutableStateOf(true) }

    var userList by remember { mutableStateOf(listOf<UserItem>()) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        newImageUri = uri
    }

    LaunchedEffect(Unit) {
        FirebaseConfig.firestore.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { result ->
                val doc = result.documents.firstOrNull()
                if (doc != null) {
                    newUsername = doc.getString("username") ?: ""
                    image = doc.getString("image") ?: ""
                }
                isFetching = false
            }
            .addOnFailureListener {
                isFetching = false
                Toast.makeText(context, "Lỗi tải dữ liệu user", Toast.LENGTH_SHORT).show()
            }

        FirebaseConfig.firestore.collection("users")
            .get()
            .addOnSuccessListener { result ->
                userList = result.documents.map { doc ->
                    UserItem(
                        username = doc.getString("username") ?: "",
                        role = doc.getString("role") ?: "",
                        image = doc.getString("image") ?: ""
                    )
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Lỗi tải danh sách user", Toast.LENGTH_SHORT).show()
            }
    }

    fun updateProfile(finalImageUrl: String) {
        FirebaseConfig.firestore.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { result ->
                for (doc in result.documents) {
                    val updates = mutableMapOf<String, Any>(
                        "username" to newUsername,
                        "image" to finalImageUrl
                    )

                    if (password.isNotBlank()) {
                        updates["password"] = PasswordUtils.hashPassword(password)
                    }

                    doc.reference.update(updates)
                }

                isLoading = false
                Toast.makeText(context, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                isLoading = false
                Toast.makeText(context, "Cập nhật thất bại", Toast.LENGTH_SHORT).show()
            }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Bạn không có quyền admin",
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { paddingValues ->

        if (isFetching) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))



                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = when {
                            newImageUri != null -> rememberAsyncImagePainter(newImageUri)
                            image.isNotBlank() -> rememberAsyncImagePainter(image)
                            else -> rememberAsyncImagePainter("https://cdn-icons-png.flaticon.com/512/149/149071.png")
                        },
                        contentDescription = "Ảnh user",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Button(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.padding(top = 14.dp)
                ) {
                    Text("Chọn ảnh")
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = newUsername,
                    onValueChange = { newUsername = it },
                    label = { Text("Username") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Mật khẩu mới") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null)
                    },
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) {
                                    Icons.Default.Visibility
                                } else {
                                    Icons.Default.VisibilityOff
                                },
                                contentDescription = null
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (showPassword) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Xác nhận mật khẩu") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null)
                    },
                    trailingIcon = {
                        IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                            Icon(
                                imageVector = if (showConfirmPassword) {
                                    Icons.Default.Visibility
                                } else {
                                    Icons.Default.VisibilityOff
                                },
                                contentDescription = null
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = confirmPassword.isNotEmpty() && password != confirmPassword,
                    visualTransformation = if (showConfirmPassword) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    }
                )

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = {
                        if (newUsername.isBlank()) {
                            Toast.makeText(context, "Username không được để trống", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (password.isNotBlank() || confirmPassword.isNotBlank()) {
                            if (password != confirmPassword) {
                                Toast.makeText(context, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            if (password.length < 6) {
                                Toast.makeText(context, "Mật khẩu phải từ 6 ký tự", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                        }

                        isLoading = true

                        if (newImageUri != null) {
                            MediaManager.get().upload(newImageUri)
                                .unsigned("komer_unsigned")
                                .callback(object : UploadCallback {
                                    override fun onStart(requestId: String?) {}

                                    override fun onProgress(
                                        requestId: String?,
                                        bytes: Long,
                                        totalBytes: Long
                                    ) {}

                                    override fun onSuccess(
                                        requestId: String?,
                                        resultData: Map<*, *>?
                                    ) {
                                        val imageUrl = resultData?.get("secure_url")?.toString() ?: image
                                        updateProfile(imageUrl)
                                    }

                                    override fun onError(requestId: String?, error: ErrorInfo?) {
                                        isLoading = false
                                        Toast.makeText(
                                            context,
                                            "Upload ảnh thất bại: ${error?.description}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }

                                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
                                })
                                .dispatch()
                        } else {
                            updateProfile(image)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Cập nhật thông tin")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Đăng xuất / quay lại đăng nhập")
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Danh sách user",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                userList.forEach { user ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = if (user.image.isNotBlank()) {
                                    rememberAsyncImagePainter(user.image)
                                } else {
                                    rememberAsyncImagePainter("https://cdn-icons-png.flaticon.com/512/149/149071.png")
                                },
                                contentDescription = "Avatar user",
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = user.username,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Role: ${user.role}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
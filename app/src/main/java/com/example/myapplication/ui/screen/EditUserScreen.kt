package com.example.myapplication.ui.screen

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.myapplication.utils.PasswordUtils
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUserScreen(
    navController: NavController,
    username: String
) {
    var newUsername by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("user") }
    var image by remember { mutableStateOf("") }

    var newImageUri by remember { mutableStateOf<Uri?>(null) }

    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var isFetching by remember { mutableStateOf(true) }

    var expanded by remember { mutableStateOf(false) }
    val roleOptions = listOf("admin", "user")

    val context = LocalContext.current

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
                    password = ""
                    confirmPassword = ""
                    role = doc.getString("role") ?: "user"
                    image = doc.getString("image") ?: ""
                }
                isFetching = false
            }
            .addOnFailureListener {
                isFetching = false
                Toast.makeText(context, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show()
            }
    }

    fun updateUser(finalImageUrl: String) {
        FirebaseConfig.firestore.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { result ->
                for (doc in result.documents) {
                    doc.reference.update(
                        mapOf(
                            "username" to newUsername,
                            "password" to PasswordUtils.hashPassword(password),
                            "role" to role,
                            "image" to finalImageUrl
                        )
                    )
                }

                isLoading = false
                Toast.makeText(context, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            }
            .addOnFailureListener {
                isLoading = false
                Toast.makeText(context, "Cập nhật thất bại", Toast.LENGTH_SHORT).show()
            }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Chỉnh sửa User",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        if (isFetching) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
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
                    modifier = Modifier.padding(top = 14.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Chọn ảnh mới", fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.height(28.dp))

                OutlinedTextField(
                    value = newUsername,
                    onValueChange = { newUsername = it },
                    label = { Text("Username") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
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
                                contentDescription = if (showPassword) "Ẩn mật khẩu" else "Hiện mật khẩu"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
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
                                contentDescription = if (showConfirmPassword) "Ẩn mật khẩu" else "Hiện mật khẩu"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    singleLine = true,
                    isError = confirmPassword.isNotEmpty() && password != confirmPassword,
                    visualTransformation = if (showConfirmPassword) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = role,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Phân quyền") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        singleLine = true
                    )

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        roleOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    role = option
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (newUsername.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                            Toast.makeText(context, "Vui lòng không để trống", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (password != confirmPassword) {
                            Toast.makeText(context, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show()
                            return@Button
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
                                        updateUser(imageUrl)
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
                            updateUser(image)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Cập nhật thông tin",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
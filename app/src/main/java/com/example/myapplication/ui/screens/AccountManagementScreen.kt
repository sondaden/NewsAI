package com.example.myapplication.ui.screens

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.R
import com.example.myapplication.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountManagementScreen(navController: NavController) {
    val context = LocalContext.current
    val userViewModel: UserViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return UserViewModel(context) as T
            }
        }
    )
    val userData by userViewModel.userData.collectAsState()
    var username by remember { mutableStateOf(userData?.displayName ?: "John Doe") }
    var email by remember { mutableStateOf(userData?.email ?: "email@example.com") }
    var isLoading by remember { mutableStateOf(true) }
    var showUsernameDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showAvatarMenu by remember { mutableStateOf(false) }
    var showAvatarMessageDialog by remember { mutableStateOf(false) }
    var showViewAvatarDialog by remember { mutableStateOf(false) }
    var avatarMessage by remember { mutableStateOf("") }
    var avatarMessageIsSuccess by remember { mutableStateOf(true) }
    var usernameInput by remember { mutableStateOf(username) }
    var passwordForUsername by remember { mutableStateOf("") }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var usernameError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var passwordForUsernameError by remember { mutableStateOf<String?>(null) }
    var currentPasswordError by remember { mutableStateOf<String?>(null) }
    var passwordForUsernameVisible by remember { mutableStateOf(false) }
    var currentPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var avatarUri by remember { mutableStateOf<String?>(null) }
    var showUsernameMessageDialog by remember { mutableStateOf(false) }
    var usernameMessage by remember { mutableStateOf("") }
    var usernameMessageIsSuccess by remember { mutableStateOf(true) }
    var showPasswordMessageDialog by remember { mutableStateOf(false) }
    var passwordMessage by remember { mutableStateOf("") }
    var passwordMessageIsSuccess by remember { mutableStateOf(true) }
    val TAG = "AccountManagementScreen"

    // Launcher để chọn ảnh từ thư viện
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            avatarUri = uri.toString()
            Log.d(TAG, "Ảnh đại diện đã được chọn: $avatarUri")
            avatarMessage = "Ảnh đại diện đã được cập nhật!"
            avatarMessageIsSuccess = true
        } else {
            Log.w(TAG, "Không có ảnh nào được chọn")
            avatarMessage = "Không có ảnh nào được chọn!"
            avatarMessageIsSuccess = false
        }
        showAvatarMessageDialog = true
    }

    // Kiểm tra trạng thái tải dữ liệu người dùng
    LaunchedEffect(userData) {
        if (userData != null) {
            username = userData?.displayName ?: "John Doe"
            usernameInput = username
            email = userData?.email ?: "email@example.com"
            Log.d(TAG, "Updated usernameInput from userData: $username")
            isLoading = false
        }
    }

    // Hiệu ứng tải toàn màn hình
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                strokeWidth = 4.dp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    } else if (userData != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Quản lý tài khoản",
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                    }
                },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = { /* Do nothing */ }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.background, // Ẩn icon bằng cách đặt cùng màu với nền
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Thẻ chứa avatar, tên người dùng và email
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box {
                            Image(
                                painter = if (avatarUri != null) {
                                    painterResource(id = R.drawable.avatar_placeholder)
                                } else {
                                    painterResource(id = R.drawable.avatar_placeholder)
                                },
                                contentDescription = "Avatar",
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .clickable { showAvatarMenu = true }
                            )
                            // Menu tùy chọn sửa avatar
                            DropdownMenu(
                                expanded = showAvatarMenu,
                                onDismissRequest = { showAvatarMenu = false },
                                modifier = Modifier
                                    .width(200.dp)
                                    .background(MaterialTheme.colorScheme.surface)
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "Thay đổi ảnh đại diện",
                                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    onClick = {
                                        pickImageLauncher.launch("image/*")
                                        showAvatarMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "Xóa ảnh đại diện",
                                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    },
                                    onClick = {
                                        avatarUri = null
                                        avatarMessage = "Ảnh đại diện đã được xóa!"
                                        avatarMessageIsSuccess = true
                                        showAvatarMessageDialog = true
                                        showAvatarMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "Xem ảnh đại diện",
                                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    onClick = {
                                        showViewAvatarDialog = true
                                        showAvatarMenu = false
                                    }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = username,
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = email,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Thẻ Tên người dùng
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .clickable { showUsernameDialog = true },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Tên người dùng",
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = "Arrow",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Thẻ Đổi mật khẩu
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .clickable { showPasswordDialog = true },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Đổi mật khẩu",
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = "Arrow",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }

    // Dialog cho Tên người dùng
    if (showUsernameDialog) {
        Dialog(onDismissRequest = { showUsernameDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Chỉnh sửa tên người dùng",
                        style = MaterialTheme.typography.headlineSmall.copy(fontSize = 20.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    OutlinedTextField(
                        value = usernameInput,
                        onValueChange = { usernameInput = it; usernameError = null },
                        label = { Text("Tên người dùng", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp)) },
                        isError = usernameError != null,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = if (usernameError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp)
                    )
                    usernameError?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    OutlinedTextField(
                        value = passwordForUsername,
                        onValueChange = { passwordForUsername = it; passwordForUsernameError = null },
                        label = { Text("Mật khẩu", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp)) },
                        isError = passwordForUsernameError != null,
                        singleLine = true,
                        visualTransformation = if (passwordForUsernameVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordForUsernameVisible = !passwordForUsernameVisible }) {
                                Icon(
                                    imageVector = if (passwordForUsernameVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (passwordForUsernameVisible) "Ẩn mật khẩu" else "Hiện mật khẩu",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = if (usernameError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp)
                    )
                    passwordForUsernameError?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(
                            onClick = {
                                showUsernameDialog = false
                                usernameError = null
                                passwordForUsernameError = null
                                passwordForUsername = ""
                                passwordForUsernameVisible = false
                                Log.d(TAG, "Username dialog cancelled")
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp),
                        ) {
                            Text(
                                text = "Hủy",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp)
                            )
                        }

                        Button(
                            onClick = {
                                usernameError = when {
                                    usernameInput.isBlank() -> "Vui lòng nhập tên người dùng"
                                    usernameInput.length < 3 -> "Tên người dùng phải có ít nhất 3 ký tự"
                                    else -> null
                                }
                                passwordForUsernameError = when {
                                    passwordForUsername.isBlank() -> "Vui lòng nhập mật khẩu"
                                    passwordForUsername.length < 8 -> "Mật khẩu phải có ít nhất 8 ký tự"
                                    else -> null
                                }

                                Log.d(TAG, "Validation - usernameError: $usernameError, passwordForUsernameError: $passwordForUsernameError")

                                if (usernameError == null && passwordForUsernameError == null) {
                                    Log.d(TAG, "Attempting to update username to: $usernameInput")
                                    userViewModel.updateUsername(usernameInput, passwordForUsername) { result ->
                                        result.fold(
                                            onSuccess = {
                                                username = usernameInput
                                                usernameMessage = "Cập nhật tên người dùng thành công!"
                                                usernameMessageIsSuccess = true
                                                showUsernameMessageDialog = true
                                                showUsernameDialog = false
                                                passwordForUsername = ""
                                                passwordForUsernameVisible = false
                                                Log.d(TAG, "Username updated successfully to: $usernameInput")
                                                // Load lại thông tin người dùng
                                                userViewModel.fetchUserData()
                                            },
                                            onFailure = { error ->
                                                Log.e(TAG, "Failed to update username: ${error.message}", error)
                                                if (error.message?.contains("Mật khẩu không đúng") == true) {
                                                    passwordForUsernameError = "Mật khẩu không đúng"
                                                } else {
                                                    usernameError = when {
                                                        error.message?.contains("Chưa đăng nhập") == true -> "Vui lòng đăng nhập lại"
                                                        error.message?.contains("mạng") == true -> "Lỗi kết nối mạng, vui lòng thử lại"
                                                        else -> "Không thể cập nhật tên, thử lại sau"
                                                    }
                                                }
                                            }
                                        )
                                    }
                                } else {
                                    Log.w(TAG, "Validation failed: usernameError=$usernameError, passwordForUsernameError=$passwordForUsernameError")
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(
                                "Xác nhận",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog cho Đổi mật khẩu
    if (showPasswordDialog) {
        Dialog(onDismissRequest = { showPasswordDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Đổi mật khẩu",
                        style = MaterialTheme.typography.headlineSmall.copy(fontSize = 20.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it; currentPasswordError = null },
                        label = { Text("Mật khẩu hiện tại", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp)) },
                        isError = currentPasswordError != null,
                        singleLine = true,
                        visualTransformation = if (currentPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { currentPasswordVisible = !currentPasswordVisible }) {
                                Icon(
                                    imageVector = if (currentPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (currentPasswordVisible) "Ẩn mật khẩu hiện tại" else "Hiện mật khẩu hiện tại",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = if (currentPasswordError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp)
                    )
                    currentPasswordError?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it; passwordError = null },
                        label = { Text("Mật khẩu mới", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp)) },
                        isError = passwordError != null,
                        singleLine = true,
                        visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                                Icon(
                                    imageVector = if (newPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (newPasswordVisible) "Ẩn mật khẩu mới" else "Hiện mật khẩu mới",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = if (passwordError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp)
                    )
                    passwordError?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(
                            onClick = {
                                showPasswordDialog = false
                                currentPasswordError = null
                                passwordError = null
                                currentPassword = ""
                                newPassword = ""
                                currentPasswordVisible = false
                                newPasswordVisible = false
                                Log.d(TAG, "Password dialog cancelled")
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp),
                        ) {
                            Text(
                                text = "Hủy",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp)
                            )
                        }

                        Button(
                            onClick = {
                                currentPasswordError = when {
                                    currentPassword.isBlank() -> "Vui lòng nhập mật khẩu hiện tại"
                                    currentPassword.length < 8 -> "Mật khẩu hiện tại phải có ít nhất 8 ký tự"
                                    else -> null
                                }
                                passwordError = when {
                                    newPassword.isBlank() -> "Vui lòng nhập mật khẩu mới"
                                    newPassword.length < 8 -> "Mật khẩu mới cần ít nhất 8 ký tự"
                                    !newPassword.matches(".*[a-zA-Z].*".toRegex()) -> "Mật khẩu mới phải chứa ít nhất 1 chữ cái"
                                    !newPassword.matches(".*\\d.*".toRegex()) -> "Mật khẩu mới phải chứa ít nhất 1 số"
                                    newPassword == currentPassword -> "Mật khẩu mới không được trùng với mật khẩu hiện tại"
                                    else -> null
                                }

                                Log.d(TAG, "Validation - currentPasswordError: $currentPasswordError, passwordError: $passwordError")

                                if (currentPasswordError == null && passwordError == null) {
                                    Log.d(TAG, "Attempting to change password")
                                    userViewModel.changePassword(currentPassword, newPassword) { result ->
                                        result.fold(
                                            onSuccess = {
                                                passwordMessage = "Đổi mật khẩu thành công!"
                                                passwordMessageIsSuccess = true
                                                showPasswordMessageDialog = true
                                                showPasswordDialog = false
                                                currentPassword = ""
                                                newPassword = ""
                                                currentPasswordVisible = false
                                                newPasswordVisible = false
                                                Log.d(TAG, "Password changed successfully")
                                                // Load lại thông tin người dùng
                                                userViewModel.fetchUserData()
                                            },
                                            onFailure = { error ->
                                                Log.e(TAG, "Failed to change password: ${error.message}", error)
                                                if (error.message?.contains("Mật khẩu không đúng") == true) {
                                                    currentPasswordError = "Mật khẩu hiện tại không đúng"
                                                } else {
                                                    passwordError = when {
                                                        error.message?.contains("Chưa đăng nhập") == true -> "Vui lòng đăng nhập lại"
                                                        error.message?.contains("mạng") == true -> "Lỗi kết nối mạng, vui lòng thử lại"
                                                        else -> "Không thể đổi mật khẩu, thử lại sau"
                                                    }
                                                }
                                            }
                                        )
                                    }
                                } else {
                                    Log.w(TAG, "Validation failed: currentPasswordError=$currentPasswordError, passwordError=$passwordError")
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(
                                "Xác nhận",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog thông báo kết quả sửa/xóa avatar
    if (showAvatarMessageDialog) {
        Dialog(onDismissRequest = { showAvatarMessageDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (avatarMessageIsSuccess) "Thành công" else "Thất bại",
                        style = MaterialTheme.typography.headlineSmall.copy(fontSize = 20.sp),
                        color = if (avatarMessageIsSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = avatarMessage,
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = { showAvatarMessageDialog = false },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(
                            "Xác nhận",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp)
                        )
                    }
                }
            }
        }
    }

    // Dialog thông báo kết quả cập nhật tên người dùng
    if (showUsernameMessageDialog) {
        Dialog(onDismissRequest = { showUsernameMessageDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (usernameMessageIsSuccess) "Thành công" else "Thất bại",
                        style = MaterialTheme.typography.headlineSmall.copy(fontSize = 20.sp),
                        color = if (usernameMessageIsSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = usernameMessage,
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = { showUsernameMessageDialog = false },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(
                            "Xác nhận",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp)
                        )
                    }
                }
            }
        }
    }

    // Dialog thông báo kết quả đổi mật khẩu
    if (showPasswordMessageDialog) {
        Dialog(onDismissRequest = { showPasswordMessageDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (passwordMessageIsSuccess) "Thành công" else "Thất bại",
                        style = MaterialTheme.typography.headlineSmall.copy(fontSize = 20.sp),
                        color = if (passwordMessageIsSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = passwordMessage,
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = { showPasswordMessageDialog = false },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(
                            "Xác nhận",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp)
                        )
                    }
                }
            }
        }
    }

    // Dialog xem ảnh đại diện
    if (showViewAvatarDialog) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.8f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = if (avatarUri != null) {
                        painterResource(id = R.drawable.avatar_placeholder)
                    } else {
                        painterResource(id = R.drawable.avatar_placeholder)
                    },
                    contentDescription = "Xem ảnh đại diện",
                    modifier = Modifier
                        .size(300.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.height(16.dp))
                IconButton(
                    onClick = { showViewAvatarDialog = false },
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.error, CircleShape)
                ) {
                    Text(
                        text = "X",
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
package com.example.myapplication.ui.screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.R
import com.example.myapplication.viewmodel.AuthViewModel
import com.example.myapplication.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel()
) {
    val context = LocalContext.current
    var displayName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    val registerState by authViewModel.registerState.observeAsState()

    var displayNameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var generalError by remember { mutableStateOf<String?>(null) }

    // Reset lỗi chung khi người dùng thay đổi input
    LaunchedEffect(email, password, displayName, confirmPassword) {
        generalError = null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo/Icon
            Icon(
                painter = painterResource(id = R.drawable.ic_logo),
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = Color(0xFF1E88E5)
            )
            Spacer(Modifier.height(12.dp))

            // Title + Subtitle
            Text(
                "Tạo Tài Khoản",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 30.sp),
                color = Color.Black
            )
            Spacer(Modifier.height(24.dp))
            Text(
                "Nhập thông tin để bắt đầu",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 20.sp),
                color = Color.Gray
            )
            Spacer(Modifier.height(70.dp))

            // Display Name field
            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it; displayNameError = null },
                label = { Text("Tên người dùng", fontSize = 20.sp) },
                placeholder = { Text("Nhập tên người dùng", fontSize = 20.sp) },
                isError = displayNameError != null,
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp)),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1E88E5),
                    unfocusedBorderColor = if (displayNameError != null) MaterialTheme.colorScheme.error else Color.Transparent,
                    focusedLabelColor = Color(0xFF1E88E5),
                    unfocusedLabelColor = Color.Gray,
                    cursorColor = Color(0xFF1E88E5),
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedSupportingTextColor = Color.Black,
                    unfocusedSupportingTextColor = Color.Black
                ),
                textStyle = LocalTextStyle.current.copy(fontSize = 20.sp)
            )
            displayNameError?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(20.dp))

            // Email field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; emailError = null },
                label = { Text("Email", fontSize = 20.sp) },
                placeholder = { Text("Nhập email của bạn", fontSize = 20.sp) },
                isError = emailError != null || generalError != null,
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color.Gray) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp)),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1E88E5),
                    unfocusedBorderColor = if (emailError != null || generalError != null) MaterialTheme.colorScheme.error else Color.Transparent,
                    focusedLabelColor = Color(0xFF1E88E5),
                    unfocusedLabelColor = Color.Gray,
                    cursorColor = Color(0xFF1E88E5),
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedSupportingTextColor = Color.Black,
                    unfocusedSupportingTextColor = Color.Black
                ),
                textStyle = LocalTextStyle.current.copy(fontSize = 20.sp)
            )
            emailError?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(20.dp))

            // Password field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; passwordError = null },
                label = { Text("Mật khẩu", fontSize = 20.sp) },
                placeholder = { Text("Tạo mật khẩu", fontSize = 20.sp) },
                isError = passwordError != null || generalError != null,
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "Ẩn mật khẩu" else "Hiện mật khẩu",
                            tint = Color.Gray
                        )
                    }
                },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp)),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1E88E5),
                    unfocusedBorderColor = if (passwordError != null || generalError != null) MaterialTheme.colorScheme.error else Color.Transparent,
                    focusedLabelColor = Color(0xFF1E88E5),
                    unfocusedLabelColor = Color.Gray,
                    cursorColor = Color(0xFF1E88E5),
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedSupportingTextColor = Color.Black,
                    unfocusedSupportingTextColor = Color.Black
                ),
                textStyle = LocalTextStyle.current.copy(fontSize = 20.sp)
            )
            passwordError?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(20.dp))

            // Confirm Password field
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; confirmPasswordError = null },
                label = { Text("Xác nhận mật khẩu", fontSize = 20.sp) },
                placeholder = { Text("Xác nhận mật khẩu của bạn", fontSize = 20.sp) },
                isError = confirmPasswordError != null,
                singleLine = true,
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (confirmPasswordVisible) "Ẩn mật khẩu xác nhận" else "Hiện mật khẩu xác nhận",
                            tint = Color.Gray
                        )
                    }
                },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp)),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1E88E5),
                    unfocusedBorderColor = if (confirmPasswordError != null) MaterialTheme.colorScheme.error else Color.Transparent,
                    focusedLabelColor = Color(0xFF1E88E5),
                    unfocusedLabelColor = Color.Gray,
                    cursorColor = Color(0xFF1E88E5),
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedSupportingTextColor = Color.Black,
                    unfocusedSupportingTextColor = Color.Black
                ),
                textStyle = LocalTextStyle.current.copy(fontSize = 20.sp)
            )
            confirmPasswordError?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            // Hiển thị lỗi chung nếu có
            generalError?.let {
                Spacer(Modifier.height(8.dp))
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            Spacer(Modifier.height(16.dp))

            // Create Account button
            Button(
                onClick = {
                    // Ưu tiên 1: Kiểm tra lỗi input cục bộ
                    displayNameError = when {
                        displayName.isBlank() -> "Tên hiển thị không được để trống!"
                        displayName.length < 3 -> "Tên hiển thị phải có ít nhất 3 ký tự!"
                        else -> null
                    }
                    emailError = when {
                        email.isBlank() -> "Email không được để trống!"
                        !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Email không hợp lệ!"
                        else -> null
                    }
                    passwordError = when {
                        password.isBlank() -> "Mật khẩu không được để trống!"
                        password.length < 8 -> "Mật khẩu phải có ít nhất 8 ký tự!"
                        !password.matches(".*[a-zA-Z].*".toRegex()) -> "Mật khẩu phải chứa ít nhất 1 chữ cái!"
                        !password.matches(".*\\d.*".toRegex()) -> "Mật khẩu phải chứa ít nhất 1 số!"
                        else -> null
                    }
                    confirmPasswordError = when {
                        confirmPassword.isBlank() -> "Xác nhận mật khẩu không được để trống!"
                        password != confirmPassword -> "Mật khẩu xác nhận không khớp!"
                        else -> null
                    }

                    Log.d("Register", "displayNameError: $displayNameError")
                    Log.d("Register", "emailError: $emailError")
                    Log.d("Register", "passwordError: $passwordError")
                    Log.d("Register", "confirmPasswordError: $confirmPasswordError")

                    if (displayNameError == null && emailError == null && passwordError == null && confirmPasswordError == null) {
                        // Ưu tiên 2: Kiểm tra email đã tồn tại
                        authViewModel.checkEmailExists(email, context) { exists ->
                            if (exists) {
                                Log.d("Register", "Email đã tồn tại!")
                                emailError = "Email đã được đăng ký!"
                            } else {
                                Log.d("Register", "Bắt đầu đăng ký với email: $email, displayName: $displayName")
                                authViewModel.registerUser(email, password, displayName, avatarUrl = null, context)
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
            ) {
                Text("Tạo Tài Khoản", color = Color.White, style = MaterialTheme.typography.labelLarge.copy(fontSize = 20.sp))
            }
            Spacer(Modifier.height(35.dp))

            // Login link
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Đã có tài khoản?", color = Color.Black, fontSize = 22.sp)
                Spacer(Modifier.width(4.dp))
                Text(
                    "Đăng Nhập",
                    color = Color(0xFF1E88E5),
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    modifier = Modifier.clickable { navController.navigate("login") }
                )
            }
            Spacer(Modifier.height(35.dp))
        }
    }

    LaunchedEffect(registerState) {
        registerState?.let { result ->
            result.fold(
                onSuccess = {
                    Log.d("Register", "Đăng ký thành công! Yêu cầu xác nhận email.")
                    try {
                        navController.navigate("verify_email") {
                            popUpTo("register") { inclusive = true }
                        }
                    } catch (e: Exception) {
                        Log.e("Register", "Lỗi điều hướng: ${e.message}")
                        generalError = "Lỗi điều hướng, vui lòng thử lại!"
                    }
                    authViewModel.clearRegisterState()
                },
                onFailure = { error ->
                    Log.d("Register", "Đăng ký thất bại: ${error.message}")
                    val errorMessage = error.message ?: "Đăng ký thất bại! Vui lòng thử lại."
                    if (errorMessage.contains("email", ignoreCase = true)) {
                        emailError = errorMessage
                    } else if (errorMessage.contains("mật khẩu", ignoreCase = true)) {
                        passwordError = errorMessage
                    } else {
                        generalError = errorMessage
                    }
                    authViewModel.clearRegisterState()
                }
            )
        }
    }
}
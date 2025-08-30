package com.example.myapplication.ui.screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.R
import com.example.myapplication.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val loginState by authViewModel.loginState.observeAsState()

    // UI states
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var generalError by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // Reset lỗi chung khi người dùng thay đổi input
    LaunchedEffect(email, password) {
        generalError = null
    }

    // Handle login callbacks
    LaunchedEffect(loginState) {
        loginState?.fold(
            onSuccess = {
                Log.d("Login", "Đăng nhập thành công!")
                isLoading = false
                navController.navigate("home") {
                    popUpTo("login") { inclusive = true }
                }
                authViewModel.clearLoginState()
            },
            onFailure = { err ->
                Log.d("Login", "Đăng nhập thất bại: ${err.message}")
                isLoading = false
                val errorMessage = err.message ?: "Đăng nhập thất bại!"
                if (errorMessage.contains("email", ignoreCase = true) && errorMessage.contains("mật khẩu", ignoreCase = true)) {
                    generalError = errorMessage
                } else if (errorMessage.contains("email", ignoreCase = true)) {
                    emailError = errorMessage
                } else if (errorMessage.contains("mật khẩu", ignoreCase = true)) {
                    passwordError = errorMessage
                } else {
                    generalError = errorMessage
                }
                authViewModel.clearLoginState()
            }
        )
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
                modifier = Modifier.size(150.dp),
                tint = Color(0xFF1E88E5)
            )
            Spacer(Modifier.height(12.dp))

            // Title + Subtitle
            Text(
                "Chào Mừng Trở Lại",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 30.sp),
                color = Color.Black
            )
            Spacer(Modifier.height(24.dp))
            Text(
                "Đăng nhập để tiếp tục",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 20.sp),
                color = Color.Gray
            )
            Spacer(Modifier.height(70.dp))

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
                placeholder = { Text("Nhập mật khẩu của bạn", fontSize = 20.sp) },
                isError = passwordError != null || generalError != null,
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
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

            // Remember me & Forgot password
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = rememberMe,
                    onCheckedChange = { rememberMe = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFF1E88E5),
                        uncheckedColor = Color.Gray
                    )
                )
                Text("Ghi nhớ tôi", color = Color.Black, fontSize = 18.sp)
                Spacer(Modifier.weight(1f))
                TextButton(onClick = { navController.navigate("forgot_password") }) {
                    Text("Quên mật khẩu?", color = Color(0xFF1E88E5), fontSize = 18.sp)
                }
            }
            Spacer(Modifier.height(16.dp))

            // Sign In button with loading
            Button(
                onClick = {
                    emailError = when {
                        email.isBlank() -> "Email không được để trống"
                        !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Email không hợp lệ!"
                        else -> null
                    }
                    passwordError = when {
                        password.isBlank() -> "Mật khẩu không được để trống"
                        password.length < 8 -> "Mật khẩu phải tối thiểu 8 ký tự"
                        else -> null
                    }
                    if (emailError == null && passwordError == null) {
                        Log.d("Login", "Bắt đầu đăng nhập với email: $email")
                        isLoading = true
                        authViewModel.loginUser(email, password, context)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Đang đăng nhập...", color = Color.White, style = MaterialTheme.typography.labelLarge.copy(fontSize = 20.sp))
                } else {
                    Text("Đăng Nhập", color = Color.White, style = MaterialTheme.typography.labelLarge.copy(fontSize = 20.sp))
                }
            }
            Spacer(Modifier.height(35.dp))

            // Sign Up link
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Bạn chưa có tài khoản?", color = Color.Black, fontSize = 22.sp)
                Spacer(Modifier.width(4.dp))
                Text(
                    "Đăng Ký",
                    color = Color(0xFF1E88E5),
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    modifier = Modifier.clickable { navController.navigate("register") }
                )
            }
            Spacer(Modifier.height(35.dp))

            // Divider + Social login
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Divider(modifier = Modifier.weight(1f), color = Color.Gray)
                Text(
                    "Hoặc tiếp tục với",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 18.sp),
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Divider(modifier = Modifier.weight(1f), color = Color.Gray)
            }
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(
                    onClick = { /* TODO: Google Auth */ },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color.Gray)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_google),
                        contentDescription = "Google",
                        tint = Color.Unspecified
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Google", color = Color.Black, fontSize = 18.sp)
                }
                Spacer(Modifier.width(16.dp))
                OutlinedButton(
                    onClick = { /* TODO: Apple Auth */ },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color.Gray)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.avatar_placeholder),
                        contentDescription = "Apple",
                        tint = Color.Unspecified
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Apple", color = Color.Black, fontSize = 18.sp)
                }
            }
        }
    }
}
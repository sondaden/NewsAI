package com.example.myapplication.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.R
import com.example.myapplication.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var generalError by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val resetPasswordState by authViewModel.resetPasswordState.observeAsState()
    val TAG = "ForgotPasswordScreen"

    // Reset lỗi và thông báo khi thay đổi email
    LaunchedEffect(email) {
        emailError = null
        generalError = null
        successMessage = null
    }

    // Xử lý kết quả gửi email đặt lại mật khẩu
    LaunchedEffect(resetPasswordState) {
        resetPasswordState?.let { result ->
            isLoading = false
            result.fold(
                onSuccess = { message ->
                    Log.d(TAG, "Gửi email đặt lại mật khẩu thành công")
                    successMessage = message
                    authViewModel.clearResetPasswordState()
                },
                onFailure = { error ->
                    Log.d(TAG, "Gửi email đặt lại mật khẩu thất bại: ${error.message}")
                    val errorMessage = error.message ?: "Không thể gửi email, vui lòng thử lại!"
                    if (errorMessage.contains("email", ignoreCase = true)) {
                        emailError = errorMessage
                    } else {
                        generalError = errorMessage
                    }
                    authViewModel.clearResetPasswordState()
                }
            )
        }
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
            Icon(
                painter = painterResource(id = R.drawable.ic_logo),
                contentDescription = "Logo",
                modifier = Modifier.size(150.dp),
                tint = Color(0xFF1E88E5)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Title
            Text(
                text = "Quên Mật Khẩu",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold
                ),
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            Text(
                text = "Nhập địa chỉ email của bạn để nhận liên kết đặt lại mật khẩu.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(32.dp))

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
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Success or Error message
            successMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    it,
                    color = Color(0xFF4CAF50),
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
            generalError?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Send Reset Link Button
            Button(
                onClick = {
                    emailError = when {
                        email.isBlank() -> "Email không được để trống!"
                        !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Email không hợp lệ!"
                        else -> null
                    }
                    if (emailError == null) {
                        isLoading = true
                        authViewModel.sendPasswordResetEmail(email, context)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = !isLoading,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1E88E5),
                    contentColor = Color.White
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Đang gửi...",
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 20.sp),
                        color = Color.White
                    )
                } else {
                    Text(
                        "Gửi liên kết đặt lại",
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 20.sp),
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Back to Login Link
            Text(
                text = "Quay lại đăng nhập",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                color = Color(0xFF1E88E5),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate("login") {
                            popUpTo("forgot_password") { inclusive = true }
                        }
                    },
                textAlign = TextAlign.Center
            )
        }
    }
}
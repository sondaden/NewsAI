package com.example.myapplication.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.viewmodel.AuthViewModel
import com.example.myapplication.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

@Composable
fun VerifyEmailScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel()
) {
    val context = LocalContext.current
    var isResending by remember { mutableStateOf(false) }
    var showVerifiedDialog by remember { mutableStateOf(false) }
    val TAG = "VerifyEmailScreen"

    // Kiểm tra trạng thái xác thực email định kỳ (mỗi 5 giây)
    LaunchedEffect(Unit) {
        while (true) {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                Log.d(TAG, "Kiểm tra trạng thái xác thực email cho người dùng: ${user.email}")
                try {
                    user.reload() // Làm mới trạng thái người dùng
                    if (user.isEmailVerified) {
                        Log.d(TAG, "Email đã được xác thực: ${user.email}")
                        showVerifiedDialog = true
                        break
                    } else {
                        Log.d(TAG, "Email chưa được xác thực: ${user.email}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Lỗi khi kiểm tra trạng thái xác thực email: ${e.message}", e)
                }
            } else {
                Log.w(TAG, "Không có người dùng hiện tại, điều hướng về LoginScreen")
                navController.navigate("login") {
                    popUpTo("verify_email") { inclusive = true }
                }
                break
            }
            delay(3000) // Kiểm tra mỗi 5 giây
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Email,
            contentDescription = "Email",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Xác thực Email",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Chúng tôi đã gửi email xác thực đến địa chỉ email của bạn. " +
                    "Vui lòng kiểm tra hộp thư (và thư mục spam) để xác thực tài khoản của bạn.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                isResending = true
                authViewModel.resendVerificationEmail { success ->
                    isResending = false
                    if (!success) {
                        Log.w(TAG, "Không thể gửi lại email xác thực")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isResending,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            if (isResending) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Đang gửi...")
            } else {
                Text("Gửi lại email xác thực")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = {
                navController.navigate("login") {
                    popUpTo("verify_email") { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Quay lại đăng nhập")
        }
    }

    // Hiển thị dialog khi email đã được xác thực
    if (showVerifiedDialog) {
        AlertDialog(
            onDismissRequest = { /* Không cho phép tắt dialog bằng cách nhấn ngoài */ },
            title = {
                Text(
                    text = "Tài khoản đã được xác thực",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "Email của bạn đã được xác thực thành công. Vui lòng đăng nhập để tiếp tục.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        Log.d(TAG, "Người dùng xác nhận, điều hướng về LoginScreen")
                        showVerifiedDialog = false
                        navController.navigate("login") {
                            popUpTo("verify_email") { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Đăng nhập")
                }
            },
            dismissButton = {}
        )
    }
}
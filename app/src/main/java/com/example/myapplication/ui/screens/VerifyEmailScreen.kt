package com.example.myapplication.ui.screens

import android.widget.Toast
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
import androidx.navigation.NavController
import com.example.myapplication.ui.components.AppScaffold
import com.example.myapplication.viewmodel.AuthViewModel

@Composable
fun VerifyEmailScreen(navController: NavController, authViewModel: AuthViewModel) {
    val context = LocalContext.current
    var isResending by remember { mutableStateOf(false) }

    AppScaffold {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
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
            Text("Xác thực Email", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Chúng tôi đã gửi email xác thực đến địa chỉ của bạn. Vui lòng kiểm tra hộp thư (và thư mục spam).",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    isResending = true
                    authViewModel.resendVerificationEmail { success ->
                        isResending = false
                        Toast.makeText(
                            context,
                            if (success) "Đã gửi lại email xác thực!" else "Gửi lại thất bại!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isResending
            ) {
                if (isResending) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                else Text("Gửi lại email xác thực")
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = { navController.navigate("login") { popUpTo("verify_email") { inclusive = true } } },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Quay lại đăng nhập")
            }
        }
    }
}
package com.example.myapplication.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.viewmodel.AuthUiState
import com.example.myapplication.viewmodel.AuthViewModel

@Composable
fun LoginScreen(navController: NavController, authViewModel: AuthViewModel) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    val loginState by authViewModel.loginState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Đăng nhập", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; emailError = null },
            label = { Text("Email") },
            isError = emailError != null,
            modifier = Modifier.fillMaxWidth()
        )
        emailError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; passwordError = null },
            label = { Text("Mật khẩu") },
            isError = passwordError != null,
            modifier = Modifier.fillMaxWidth()
        )
        passwordError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                emailError = when {
                    email.isBlank() -> "Email không được để trống"
                    !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Email không hợp lệ"
                    else -> null
                }
                passwordError = if (password.length < 8 || !password.any { it.isLetter() } || !password.any { it.isDigit() })
                    "Mật khẩu phải có ít nhất 8 ký tự gồm chữ và số!" else null

                if (emailError == null && passwordError == null) {
                    authViewModel.loginUser(email, password, context)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = loginState !is AuthUiState.Loading
        ) {
            if (loginState is AuthUiState.Loading) CircularProgressIndicator(modifier = Modifier.size(24.dp))
            else Text("Đăng nhập")
        }

        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = { navController.navigate("register") }) {
            Text("Chưa có tài khoản? Đăng ký ngay")
        }

        LaunchedEffect(loginState) {
            when (loginState) {
                is AuthUiState.Success -> {
                    Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                    navController.navigate("home") { popUpTo("login") { inclusive = true } }
                }
                is AuthUiState.Error -> {
                    Toast.makeText(context, (loginState as AuthUiState.Error).message, Toast.LENGTH_SHORT).show()
                }
                else -> Unit
            }
        }
    }
}
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
fun RegisterScreen(navController: NavController, authViewModel: AuthViewModel) {
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var usernameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    val registerState by authViewModel.registerState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Đăng ký", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it; usernameError = null },
            label = { Text("Tên người dùng") },
            isError = usernameError != null,
            modifier = Modifier.fillMaxWidth()
        )
        usernameError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

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

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it; confirmPasswordError = null },
            label = { Text("Xác nhận mật khẩu") },
            isError = confirmPasswordError != null,
            modifier = Modifier.fillMaxWidth()
        )
        confirmPasswordError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                usernameError = if (username.isBlank()) "Tên người dùng không được để trống!" else null
                emailError = when {
                    email.isBlank() -> "Email không được để trống!"
                    !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Email không hợp lệ!"
                    else -> null
                }
                passwordError = if (password.length < 8 || !password.any { it.isLetter() } || !password.any { it.isDigit() })
                    "Mật khẩu phải có ít nhất 8 ký tự gồm chữ và số!" else null
                confirmPasswordError = if (password != confirmPassword) "Mật khẩu xác nhận không khớp!" else null

                if (listOf(usernameError, emailError, passwordError, confirmPasswordError).all { it == null }) {
                    authViewModel.checkEmailExists(email) { exists ->
                        if (exists) emailError = "Email đã được đăng ký!"
                        else authViewModel.registerUser(email, password, username, null)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = registerState !is AuthUiState.Loading
        ) {
            if (registerState is AuthUiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text("Đăng ký")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = { navController.navigate("login") }) {
            Text("Đã có tài khoản? Đăng nhập")
        }

        LaunchedEffect(registerState) {
            when (registerState) {
                is AuthUiState.Success -> {
                    Toast.makeText(context, "Vui lòng kiểm tra email để xác nhận!", Toast.LENGTH_SHORT).show()
                    navController.navigate("verify_email") { popUpTo("register") { inclusive = true } }
                }
                is AuthUiState.Error -> {
                    Toast.makeText(context, (registerState as AuthUiState.Error).message, Toast.LENGTH_SHORT).show()
                }
                else -> Unit
            }
        }
    }
}
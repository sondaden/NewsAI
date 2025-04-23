package com.example.myapplication.ui.screens

import android.content.Context // Import đúng android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.R
import com.example.myapplication.repository.UserRepository
import com.example.myapplication.ui.components.AppScaffold
import com.example.myapplication.ui.components.BottomNavigationBar
import com.example.myapplication.ui.components.TopBar
import com.example.myapplication.viewmodel.AuthViewModel
import com.example.myapplication.viewmodel.UserViewModel

@Composable
fun AccountScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel
) {
    val context = LocalContext.current
    val userRepository = remember { UserRepository(context) } // Lấy UserRepository ngoài lambda
    val userProfile by authViewModel.userProfile.collectAsState()
    val isDarkTheme by userViewModel.isDarkTheme.collectAsState()

    AppScaffold(
        topBar = { TopBar(titleContent = { Text("Tài khoản", style = MaterialTheme.typography.titleLarge) }) },
        bottomBar = {
            BottomNavigationBar(
                selectedItem = "Tài khoản",
                onItemSelected = { route -> navController.navigate(route.lowercase()) }
            )
        }
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = userProfile?.avatarUrl?.let { rememberAsyncImagePainter(it) }
                            ?: painterResource(R.drawable.avatar_placeholder),
                        contentDescription = "Avatar",
                        modifier = Modifier.size(80.dp).clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(userProfile?.displayName ?: "Người dùng", style = MaterialTheme.typography.bodyLarge)
                        Text(userProfile?.email ?: "email@example.com", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            AccountOption(
                icon = Icons.Default.DarkMode,
                title = "Chế độ tối",
                trailing = {
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = { userViewModel.toggleDarkTheme(userRepository) } // Truyền userRepository trực tiếp
                    )
                }
            )
            AccountOption(
                icon = Icons.Default.History,
                title = "Lịch sử đọc",
                onClick = { navController.navigate("reading_history") }
            )
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    authViewModel.logoutUser(context)
                    Toast.makeText(context, "Đăng xuất thành công!", Toast.LENGTH_SHORT).show()
                    navController.navigate("login") { popUpTo("home") { inclusive = true } }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Đăng xuất")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Đăng xuất")
            }
        }
    }
}

@Composable
fun AccountOption(
    icon: ImageVector,
    title: String,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null, onClick = { onClick?.invoke() }),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            trailing?.invoke() ?: onClick?.let {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = "Mở",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// Extension để lấy UserRepository từ Context (không cần @Composable nếu dùng ngoài lambda)
fun Context.userRepository(): UserRepository = UserRepository(this)
package com.example.myapplication.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic

@Composable
fun BottomNavigationBar(selectedItem: String, onItemSelected: (String) -> Unit) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.onPrimary
    ) {
        val navigationItems = listOf(
            NavigationItem("Trang chủ", "home", Icons.Default.Home),
            NavigationItem("Podcast", "podcast", Icons.Default.Mic),
            NavigationItem("Chatbot", "chatbot", Icons.AutoMirrored.Filled.Chat),
            NavigationItem("Đã lưu", "saved", Icons.Default.Bookmark),
            NavigationItem("Tài khoản", "account", Icons.Default.AccountCircle)
        )

        navigationItems.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        item.icon,
                        contentDescription = item.label,
                        tint = if (selectedItem == item.route)
                            MaterialTheme.colorScheme.onBackground
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                label = {
                    Text(
                        item.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selectedItem == item.route)
                            MaterialTheme.colorScheme.onBackground
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                selected = selectedItem == item.route,
                onClick = { onItemSelected(item.route) }
            )
        }
    }
}
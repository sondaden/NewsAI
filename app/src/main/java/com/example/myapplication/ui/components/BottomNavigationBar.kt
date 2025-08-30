package com.example.myapplication.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.example.myapplication.R

data class BottomNavItem(val label: String, val route: String, val icon: Int)

@Composable
fun BottomNavigationBar(selectedItem: String, onItemSelected: (String) -> Unit) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.onPrimary
    ) {
        val navigationItems = listOf(
            BottomNavItem("Trang chủ", "home", R.drawable.ic_home),
            BottomNavItem("Chatbot", "chatbot", R.drawable.ic_chatbot),
            BottomNavItem("Đã lưu", "saved", R.drawable.ic_bookmark),
            BottomNavItem("Tài khoản", "account", R.drawable.ic_setting)
        )

        navigationItems.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(id = item.icon),
                        contentDescription = item.label,
                        modifier = Modifier
                            .padding(8.dp)
                            .background(
                                color = if (selectedItem == item.route) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent,
                                shape = CircleShape
                            )
                            .padding(8.dp)
                            .size(32.dp),
                        tint = if (selectedItem == item.route)
                            Color(0xFF1976D2)
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                selected = selectedItem == item.route,
                onClick = { onItemSelected(item.route) }
            )
        }
    }
}
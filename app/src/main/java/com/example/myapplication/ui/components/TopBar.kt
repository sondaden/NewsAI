package com.example.myapplication.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    navigationIcon: @Composable (() -> Unit)? = null,
    titleContent: @Composable () -> Unit = {
        Text(
            text = "News AI",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    },
    actions: @Composable () -> Unit = {
//        Row {
//            IconButton(onClick = {}) {
//                Icon(
//                    Icons.Default.Search,
//                    contentDescription = "Tìm kiếm",
//                    tint = MaterialTheme.colorScheme.onBackground
//                )
//            }
//            IconButton(onClick = {}) {
//                Icon(
//                    Icons.Default.Menu,
//                    contentDescription = "Menu",
//                    tint = MaterialTheme.colorScheme.onBackground
//                )
//            }
//        }
    }
) {
    TopAppBar(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp),
        windowInsets = WindowInsets(0, 0, 0, 0),
        title = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 34.dp)
                    .height(76.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon điều hướng
                navigationIcon?.invoke()

                // Tiêu đề tùy chỉnh
                titleContent()

                // Các icon hoặc thành phần khác tùy chỉnh
                actions()
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}
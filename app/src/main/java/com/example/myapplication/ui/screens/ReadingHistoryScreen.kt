package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.data.model.NewsArticle
import com.example.myapplication.ui.components.AppScaffold
import com.example.myapplication.ui.components.BottomNavigationBar
import com.example.myapplication.ui.components.TopBar
import com.example.myapplication.viewmodel.UserViewModel
import kotlinx.coroutines.launch

/**
 * Màn hình hiển thị lịch sử đọc của người dùng.
 * @param navController Điều khiển điều hướng giữa các màn hình.
 * @param userViewModel ViewModel quản lý dữ liệu người dùng.
 */
@Composable
fun ReadingHistoryScreen(
    navController: NavController,
    userViewModel: UserViewModel
) {
    val historyArticles by userViewModel.readingHistory.collectAsState()
    val scope = rememberCoroutineScope()

    // Tải dữ liệu người dùng khi khởi tạo màn hình
    LaunchedEffect(Unit) {
        userViewModel.loadUserData() // Sửa từ loadUserProfile thành loadUserData
    }

    AppScaffold(
        topBar = {
            TopBar(
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                titleContent = { Text("Lịch sử đọc", style = MaterialTheme.typography.titleMedium) }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                selectedItem = "Lịch sử đọc",
                onItemSelected = { route -> navController.navigate(route.lowercase()) }
            )
        }
    ) {
        when {
            historyArticles.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Chưa có lịch sử đọc", style = MaterialTheme.typography.bodyLarge)
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(historyArticles) { article ->
                            ReadingHistoryItem(
                                article = article,
                                onDelete = {
                                    scope.launch {
                                        userViewModel.removeFromReadingHistory(article.id)
                                    }
                                }
                            )
                        }
                    }
                    Button(
                        onClick = {
                            scope.launch { userViewModel.clearReadingHistory() }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Xóa lịch sử đọc")
                    }
                }
            }
        }
    }
}

/**
 * Hiển thị một mục trong lịch sử đọc.
 * @param article Bài viết trong lịch sử.
 * @param onDelete Hàm gọi khi xóa bài viết khỏi lịch sử.
 */
@Composable
fun ReadingHistoryItem(
    article: NewsArticle,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${article.source} • ${article.publishedAt}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Xóa",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.data.model.NewsCategory
import com.example.myapplication.ui.components.BottomNavigationBar
import com.example.myapplication.ui.components.NewsArticleItem
import com.example.myapplication.ui.components.ReusableTabBar
import com.example.myapplication.ui.components.TopBar
import com.example.myapplication.viewmodel.UserViewModel

@Composable
fun SavedArticlesScreen(navController: NavController, userViewModel: UserViewModel = viewModel()) {
    var selectedItem by remember { mutableStateOf("Đã lưu") }
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    val savedArticles by userViewModel.savedArticles.collectAsState()

    // Lấy danh sách danh mục từ bài viết đã lưu và ánh xạ sang tiếng Việt
    val categories = remember(savedArticles) {
        savedArticles.flatMap { it.category ?: emptyList() }
            .distinct()
            .filter { category ->
                savedArticles.any { article ->
                    article.category?.contains(category) == true
                }
            }
            .mapNotNull { apiValue ->
                NewsCategory.fromApiValue(apiValue)?.let { it.displayName }
            }
    }

    // Thêm tab "Tất cả" vào đầu danh sách
    val tabTitles = remember(categories) {
        listOf("Tất cả") + categories
    }

    LaunchedEffect(Unit) {
        userViewModel.fetchSavedArticles()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopBar(
            titleContent = {
                Text(
                    text = "Đã lưu",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            actions = {
                IconButton(onClick = { showDeleteAllDialog = true }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Xóa tất cả bài viết đã lưu",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (savedArticles.isNotEmpty()) {
                ReusableTabBar(tabTitles = tabTitles) { page ->
                    if (page == 0) {
                        // Tab "Tất cả": Hiển thị toàn bộ bài viết
                        SavedNewsContent(navController, userViewModel, null)
                    } else {
                        // Các tab danh mục khác
                        val selectedCategory = tabTitles[page]
                        SavedNewsContent(navController, userViewModel, selectedCategory)
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Chưa có tin tức nào được lưu",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        BottomNavigationBar(
            selectedItem = selectedItem,
            onItemSelected = { item ->
                selectedItem = item
                navController.navigate(item.lowercase()) {
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
    }

    // Dialog xác nhận xóa tất cả bài viết
    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text("Xóa tất cả bài viết đã lưu") },
            text = { Text("Bạn có chắc muốn xóa tất cả bài viết đã lưu? Hành động này không thể hoàn tác.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        userViewModel.deleteAllSavedArticles()
                        showDeleteAllDialog = false
                    }
                ) {
                    Text("Xác nhận", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
fun SavedNewsContent(navController: NavController, userViewModel: UserViewModel, categoryDisplayName: String?) {
    val articles by userViewModel.savedArticles.collectAsState()
    val filteredArticles = if (categoryDisplayName == null) {
        // Tab "Tất cả": Hiển thị toàn bộ bài viết
        articles
    } else {
        // Các tab danh mục: Lọc theo danh mục
        val categoryApiValue = NewsCategory.entries.find { it.displayName == categoryDisplayName }?.apiValue
        articles.filter { article ->
            categoryApiValue?.let { apiValue ->
                article.category?.contains(apiValue) == true
            } ?: false
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        items(filteredArticles) { article ->
            NewsArticleItem(
                article = article,
                onClick = { navController.navigate("detail/${article.id}") },
                onDelete = { userViewModel.removeSavedArticle(article.id) },
                userViewModel = userViewModel
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 12.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
            )
        }

        if (filteredArticles.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (categoryDisplayName == null) "Chưa có tin tức nào được lưu" else "Chưa có tin tức nào trong danh mục này",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
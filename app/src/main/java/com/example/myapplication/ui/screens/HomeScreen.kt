package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.data.model.NewsCategory
import com.example.myapplication.repository.NewsResult
import com.example.myapplication.ui.components.BottomNavigationBar
import com.example.myapplication.ui.components.ReusableTabBar
import com.example.myapplication.ui.components.TopBar
import com.example.myapplication.ui.components.NewsArticleItem
import com.example.myapplication.viewmodel.NewsViewModel
import com.example.myapplication.viewmodel.UserViewModel
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    navController: NavController,
    newsViewModel: NewsViewModel,
    userViewModel: UserViewModel
) {
    var selectedItem by remember { mutableStateOf("Trang chủ") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopBar(
            titleContent = {
                Text(
                    text = "News AI",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            actions = {
                Row {
                    IconButton(onClick = { navController.navigate("search") }) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Tìm kiếm",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    IconButton(onClick = { userViewModel.toggleDarkMode() }) {
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .background(MaterialTheme.colorScheme.background)
        ) {
            val homeTabs = NewsCategory.getAllDisplayNames()
            ReusableTabBar(tabTitles = homeTabs) { page ->
                val selectedCategory = NewsCategory.fromDisplayName(homeTabs[page])
                NewsListScreen(
                    navController = navController,
                    viewModel = newsViewModel,
                    userViewModel = userViewModel,
                    category = selectedCategory,
                    displayCategory = selectedCategory?.displayName
                )
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
}

@Composable
fun NewsListScreen(
    navController: NavController,
    viewModel: NewsViewModel,
    userViewModel: UserViewModel,
    category: NewsCategory? = null,
    displayCategory: String? = null
) {
    val newsState by viewModel.getNewsState(category).collectAsState()
    val isFetchingMore by viewModel.getFetchingMoreState(category).collectAsState()

    val listState = rememberLazyListState()
    val isAtBottom by remember {
        derivedStateOf {
            val layout = listState.layoutInfo
            val visibleItems = layout.visibleItemsInfo
            visibleItems.isNotEmpty() &&
                    visibleItems.last().index >= layout.totalItemsCount - 1
        }
    }

    LaunchedEffect(isAtBottom) {
        if (isAtBottom && newsState is NewsResult.Success && !isFetchingMore) {
            viewModel.fetchMoreNewsForCategory(category)
        }
    }

    LaunchedEffect(category) {
        if (category != null) {
            viewModel.fetchNewsByCategory(category)
        }
    }

    when (val state = newsState) {
        is NewsResult.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is NewsResult.Success -> {
            if (state.articles.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (category == NewsCategory.FOR_YOU)
                                "Không có bài viết đề xuất"
                            else "Không có tin tức để hiển thị",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { viewModel.reloadNews() }) {
                            Text("Tải lại")
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(state.articles) { article ->
                        NewsArticleItem(
                            article = article,
                            onClick = { navController.navigate("detail/${article.id}") },
                            userViewModel = userViewModel
                        )
                        Divider(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                        )
                    }
                    if (isFetchingMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
        is NewsResult.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    state.code?.let {
                        Text(
                            text = "Mã lỗi: HTTP $it",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { viewModel.reloadNews() }) {
                        Text("Tải lại")
                    }
                }
            }
        }
    }
}

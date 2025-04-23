package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.data.model.NewsArticle
import com.example.myapplication.ui.components.AppScaffold
import com.example.myapplication.ui.components.BottomNavigationBar
import com.example.myapplication.ui.components.ReusableTabBar
import com.example.myapplication.ui.components.TopBar
import com.example.myapplication.viewmodel.NewsUiState
import com.example.myapplication.viewmodel.NewsViewModel

@Composable
fun HomeScreen(navController: NavController, viewModel: NewsViewModel) {
    AppScaffold(
        topBar = {
            TopBar(
                titleContent = { Text("News AI", style = MaterialTheme.typography.titleLarge) },
                actions = {
                    IconButton(onClick = { /* TODO: Tìm kiếm */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Tìm kiếm")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                selectedItem = "Trang chủ",
                onItemSelected = { route -> navController.navigate(route.lowercase()) }
            )
        }
    ) {
        val tabs = listOf("Mới nhất", "Công nghệ", "Khoa học", "Thể thao", "Giải trí", "Sức khỏe")
        ReusableTabBar(tabTitles = tabs) { page ->
            NewsListScreen(navController, viewModel, tabs[page])
        }
    }
}

@Composable
fun NewsListScreen(navController: NavController, viewModel: NewsViewModel, category: String) {
    val newsState by viewModel.newsState.collectAsState()
    val lazyListState = rememberLazyListState()
    var isLoadingMore by remember { mutableStateOf(false) }

    LaunchedEffect(category) {
        viewModel.fetchNews(category)
    }

    LazyColumn(state = lazyListState, modifier = Modifier.fillMaxSize()) {
        when (val state = newsState) {
            is NewsUiState.Initial, is NewsUiState.Loading -> {
                item {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
            is NewsUiState.Success -> {
                items(state.articles) { article ->
                    NewsArticleItem(article) { navController.navigate("detail/${article.id}") }
                }
                item {
                    if (state.isLoadingMore) {
                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else if (state.articles.isNotEmpty()) {
                        LaunchedEffect(Unit) {
                            isLoadingMore = true
                            viewModel.fetchNews(category, loadMore = true)
                            isLoadingMore = false
                        }
                    }
                }
            }
            is NewsUiState.Error -> {
                item {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.message, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}

@Composable
fun NewsArticleItem(article: NewsArticle, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = article.title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 2
            )
            Text(
                text = "${article.source} • ${article.publishedAt}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
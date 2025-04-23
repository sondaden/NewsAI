package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.ui.components.BottomNavigationBar
import com.example.myapplication.ui.components.ReusableTabBar
import com.example.myapplication.ui.components.TopBar
import com.example.myapplication.ui.theme.NewsAppTheme
import com.example.myapplication.viewmodel.SavedViewModel

@Composable
fun SavedArticlesScreen(navController: NavController, savedViewModel: SavedViewModel = viewModel()) {
    var isDarkTheme by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf("Đã lưu") }

    LaunchedEffect(Unit) {
        savedViewModel.fetchSavedArticles()
    }

    NewsAppTheme(darkTheme = isDarkTheme) {
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
                    IconButton(onClick = { /* Xử lý menu */ }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = MaterialTheme.colorScheme.onBackground,
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
                val savedTabs = listOf("Chủ đề", "Nguồn", "Tin tức")
                ReusableTabBar(tabTitles = savedTabs) { page ->
                    when (page) {
                        0 -> SavedTopicsContent(navController, savedViewModel)
                        1 -> SavedSourcesContent(navController, savedViewModel)
                        2 -> SavedNewsContent(navController, savedViewModel)
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
    }
}

@Composable
fun SavedTopicsContent(navController: NavController, viewModel: SavedViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Chủ đề đã lưu",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        Text("Chức năng này sẽ được triển khai sau") // Placeholder
    }
}

@Composable
fun SavedSourcesContent(navController: NavController, viewModel: SavedViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Nguồn đã lưu",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        Text("Chức năng này sẽ được triển khai sau") // Placeholder
    }
}

@Composable
fun SavedNewsContent(navController: NavController, viewModel: SavedViewModel) {
    val articles by viewModel.savedArticles.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        items(articles) { article ->
            NewsArticleItem(article, onClick = { navController.navigate("detail/${article.id}") })
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 12.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
            )
        }

        if (articles.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Chưa có tin tức nào được lưu")
                }
            }
        }
    }
}
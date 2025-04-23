package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.data.model.NewsArticle
import com.example.myapplication.ui.components.AppScaffold
import com.example.myapplication.ui.components.TopBar
import com.example.myapplication.viewmodel.NewsViewModel

@Composable
fun DetailScreen(
    articleId: String,
    navController: NavController,
    newsViewModel: NewsViewModel
) {
    val article by produceState(initialValue = null as NewsArticle?, producer = {
        value = newsViewModel.getArticleById(articleId)
    })

    AppScaffold(
        topBar = {
            TopBar(
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                titleContent = { Text("Chi tiết tin tức") }
            )
        }
    ) {
        article?.let {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text(it.title, style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("${it.source} • ${it.publishedAt}", style = MaterialTheme.typography.bodyMedium)
                it.content?.let { content ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(content, style = MaterialTheme.typography.bodyLarge)
                }
            }
        } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Không tìm thấy bài viết", style = MaterialTheme.typography.bodyLarge)
        }
    }
}
package com.example.myapplication.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import com.example.myapplication.ui.theme.*
import com.example.myapplication.ui.components.TopBar
import com.example.myapplication.ui.components.BottomNavigationBar
import com.example.myapplication.ui.components.SectionContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PodcastScreen(navController: NavController) {
    var selectedItem by remember { mutableStateOf("Podcast") }

    Column(modifier = Modifier.fillMaxSize()) {
        TopBar(
            titleContent = {
                Text(
                    text = "Podcast",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            actions = {
                Row {
                    IconButton(onClick = { /* Xử lý tìm kiếm */ }) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Tìm kiếm",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(onClick = {  }) {
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        )

        val tabs = listOf("Mới nhất", "Công nghệ", "Khoa học", "Thể thao", "Giải trí", "Sức khỏe")
        val pagerState = rememberPagerState(pageCount = { tabs.size })
        val scope = rememberCoroutineScope()

        Column(modifier = Modifier.weight(1f)) {
            ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                edgePadding = 0.dp,
                containerColor = MaterialTheme.colorScheme.background
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (pagerState.currentPage == index)
                                    MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        selected = pagerState.currentPage == index,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        }
                    )
                }
            }

            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                when (page) {
                    0 -> LatestPodcastContent(navController)
                    1 -> TechPodcastContent()
                    2 -> SciencePodcastContent()
                    3 -> SportsPodcastContent()
                    4 -> EntertainmentPodcastContent()
                    5 -> HealthPodcastContent()
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

@Composable
fun LatestPodcastContent(navController: NavController) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Podcast mới nhất",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(8.dp))

        val podcasts = listOf("Podcast 1", "Podcast 2", "Podcast 3")
        podcasts.forEachIndexed { index, title ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clickable { navController.navigate("podcast_detail/$index") }
            ) {
                Text(
                    text = title,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
fun TechPodcastContent() { SectionContent("Podcast công nghệ") }
@Composable
fun SciencePodcastContent() { SectionContent("Podcast khoa học") }
@Composable
fun SportsPodcastContent() { SectionContent("Podcast thể thao") }
@Composable
fun EntertainmentPodcastContent() { SectionContent("Podcast giải trí") }
@Composable
fun HealthPodcastContent() { SectionContent("Podcast sức khỏe") }

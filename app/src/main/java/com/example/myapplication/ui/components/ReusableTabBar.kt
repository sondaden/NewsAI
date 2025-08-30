package com.example.myapplication.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.model.NewsCategory
import kotlinx.coroutines.launch

@Composable
fun ReusableTabBar(
    tabTitles: List<String>,
    content: @Composable (Int) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { tabTitles.size })
    val scope = rememberCoroutineScope()

    // Map các danh mục với icon từ PreferenceManagementScreen
    val categoryIcons = mapOf(
        "Mới nhất" to Icons.Default.Update,         // Icon tùy ý cho "Mới nhất"
        "Dành cho bạn" to Icons.Default.Star,       // Icon ngôi sao cho "Dành cho bạn"
        "Nổi bật" to Icons.Default.Whatshot,        // Icon ngọn lửa cho "Nổi bật"
        "Kinh doanh" to Icons.Default.Business,
        "Giải trí" to Icons.Default.TheaterComedy,
        "Môi trường" to Icons.Default.Eco,
        "Ẩm thực" to Icons.Default.Restaurant,
        "Sức khỏe" to Icons.Default.Favorite,
        "Chính trị" to Icons.Default.AccountBalance,
        "Khoa học" to Icons.Default.Science,
        "Thể thao" to Icons.Default.Sports,
        "Công nghệ" to Icons.Default.Computer,
        "Thế giới" to Icons.Default.Language,
        "Du lịch" to Icons.Default.Flight,
        "Giáo dục" to Icons.Default.School,
        "Thời trang" to Icons.Default.Checkroom,
        "Phong cách sống" to Icons.Default.Spa,
        "Ô tô" to Icons.Default.DirectionsCar,
        "Tài chính" to Icons.Default.Money,
        "Trò chơi" to Icons.Default.SportsEsports,
        "Thời tiết" to Icons.Default.Cloud,
        "Tội phạm" to Icons.Default.Gavel,
        "Văn hóa" to Icons.Default.Public,
        "Lịch sử" to Icons.Default.History,
        "Âm nhạc" to Icons.Default.MusicNote,
        "Phim ảnh" to Icons.Default.Movie
    )

    Column(modifier = Modifier.fillMaxSize()) {
        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            edgePadding = 16.dp, // Thêm padding hai bên
            containerColor = MaterialTheme.colorScheme.background,
            divider = {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                )
            },
            indicator = { /* Bỏ hiệu ứng đường kẻ xanh */ },
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            tabTitles.forEachIndexed { index, title ->
                val isSelected = pagerState.currentPage == index
                Tab(
                    modifier = Modifier
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                        .clip(RoundedCornerShape(16.dp)) // Bo góc
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surface
                        ),
                    selected = isSelected,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = categoryIcons[title] ?: Icons.Default.Info, // Fallback icon
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold ,fontSize = 16.sp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            content(page)
        }
    }
}

@Composable
fun SectionContent(title: String) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
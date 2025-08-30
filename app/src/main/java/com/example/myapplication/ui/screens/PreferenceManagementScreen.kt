package com.example.myapplication.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.TheaterComedy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.R
import com.example.myapplication.ui.components.TopBar
import com.example.myapplication.viewmodel.PreferenceViewModel

@Composable
fun PreferenceManagementScreen(
    navController: NavController,
    preferenceViewModel: PreferenceViewModel
) {
    // Danh sách các sở thích và icon tương ứng
    val preferences = listOf(
        "Phim ảnh" to Icons.Default.Movie,
        "Âm nhạc" to Icons.Default.MusicNote,
        "Lịch sử" to Icons.Default.History,
        "Văn hóa" to Icons.Default.Public,
        "Tội phạm" to Icons.Default.Gavel,
        "Thời tiết" to Icons.Default.Cloud,
        "Trò chơi" to Icons.Default.SportsEsports,
        "Tài chính" to Icons.Default.Money,
        "Ô tô" to Icons.Default.DirectionsCar,
        "Phong cách sống" to Icons.Default.Spa,
        "Thời trang" to Icons.Default.Checkroom,
        "Giáo dục" to Icons.Default.School,
        "Du lịch" to Icons.Default.Flight,
        "Thế giới" to Icons.Default.Language,
        "Công nghệ" to Icons.Default.Computer,
        "Thể thao" to Icons.Default.Sports,
        "Khoa học" to Icons.Default.Science,
        "Chính trị" to Icons.Default.AccountBalance,
        "Sức khỏe" to Icons.Default.Favorite,
        "Ẩm thực" to Icons.Default.Restaurant,
        "Môi trường" to Icons.Default.Eco,
        "Giải trí" to Icons.Default.TheaterComedy,
        "Kinh doanh" to Icons.Default.Business
    )

    // State để theo dõi sở thích đã chọn
    val selectedPreferences = remember { mutableStateOf(setOf<String>()) }

    // Tải sở thích đã lưu từ Firestore khi mở màn hình
    LaunchedEffect(Unit) {
        Log.d("PreferenceScreen", "Bắt đầu tải sở thích đã lưu...")
        preferenceViewModel.loadUserPreferences(selectedPreferences)
        Log.d("PreferenceScreen", "Hoàn tất tải sở thích, danh sách: ${selectedPreferences.value}")
    }

    // Hiển thị dialog thông báo
    if (preferenceViewModel.showDialog.value) {
        AlertDialog(
            onDismissRequest = { preferenceViewModel.showDialog.value = false },
            title = { Text("Thông báo") },
            text = { Text(preferenceViewModel.dialogMessage.value) },
            confirmButton = {
                Button(
                    onClick = { preferenceViewModel.showDialog.value = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Đồng ý", color = MaterialTheme.colorScheme.onPrimary)
                }
            },
            dismissButton = {}
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopBar(
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Quay lại",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            titleContent = {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "Sở thích",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            actions = {
                IconButton(onClick = {
                    // Lưu sở thích vào Firestore
                    preferenceViewModel.saveUserPreferences(categories = selectedPreferences.value.toList())
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_tick_xanh),
                        contentDescription = "Lưu",
                        tint = Color.Unspecified,
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
            if (preferenceViewModel.isLoading.value) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .padding(top = 20.dp)
                ) {
                    Text(
                        text = "Chọn các chủ đề bạn quan tâm",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 20.sp),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 20.dp)
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        items(preferences.size) { index ->
                            val (preference, icon) = preferences[index]
                            Card(
                                modifier = Modifier
                                    .clickable {
                                        selectedPreferences.value = if (selectedPreferences.value.contains(preference)) {
                                            selectedPreferences.value - preference
                                        } else {
                                            selectedPreferences.value + preference
                                        }
                                    },
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedPreferences.value.contains(preference)) MaterialTheme.colorScheme.primaryContainer else Color(0xFFF5F5F5)
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (selectedPreferences.value.contains(preference)) MaterialTheme.colorScheme.onPrimaryContainer else Color.Gray,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = preference,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
                                        color = if (selectedPreferences.value.contains(preference)) MaterialTheme.colorScheme.onPrimaryContainer else Color.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
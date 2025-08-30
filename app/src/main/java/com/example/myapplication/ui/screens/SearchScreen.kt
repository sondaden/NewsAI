package com.example.myapplication.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.repository.SearchHistoryEntry
import com.example.myapplication.repository.SearchRepository
import com.example.myapplication.viewmodel.NewsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(navController: NavController, newsViewModel: NewsViewModel = viewModel()) {
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var searchHistory by remember { mutableStateOf<List<SearchHistoryEntry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val searchRepository = remember { SearchRepository() }
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }
    var showClearAllDialog by remember { mutableStateOf(false) }

    // Tải toàn bộ lịch sử tìm kiếm
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        scope.launch {
            searchRepository.getSearchHistory().fold(
                onSuccess = { (history, _) ->
                    searchHistory = history
                    isLoading = false
                },
                onFailure = { error ->
                    Toast.makeText(context, error.message ?: "Lỗi khi tải lịch sử tìm kiếm", Toast.LENGTH_SHORT).show()
                    isLoading = false
                }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
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
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { newValue -> searchQuery = newValue },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .focusRequester(focusRequester),
                            placeholder = {
                                Text(
                                    "Tìm kiếm tin tức...",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Tìm kiếm",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.text.isNotEmpty()) {
                                    IconButton(
                                        onClick = { searchQuery = TextFieldValue("") }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Xóa",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(15.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                cursorColor = MaterialTheme.colorScheme.primary
                            ),
                            textStyle = MaterialTheme.typography.bodyMedium,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    if (searchQuery.text.isNotBlank()) {
                                        scope.launch {
                                            navController.navigate("search_result/${searchQuery.text}")
                                            searchRepository.saveSearchQuery(searchQuery.text)
                                            searchRepository.getSearchHistory().fold(
                                                onSuccess = { (history, _) ->
                                                    searchHistory = history
                                                },
                                                onFailure = { error ->
                                                    Toast.makeText(context, error.message ?: "Lỗi khi tải lịch sử tìm kiếm", Toast.LENGTH_SHORT).show()
                                                }
                                            )
                                        }
                                    }
                                }
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Tìm kiếm",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable {
                                    if (searchQuery.text.isNotBlank()) {
                                        scope.launch {
                                            navController.navigate("search_result/${searchQuery.text}")
                                            searchRepository.saveSearchQuery(searchQuery.text)
                                            searchRepository.getSearchHistory().fold(
                                                onSuccess = { (history, _) ->
                                                    searchHistory = history
                                                },
                                                onFailure = { error ->
                                                    Toast.makeText(context, error.message ?: "Lỗi khi tải lịch sử tìm kiếm", Toast.LENGTH_SHORT).show()
                                                }
                                            )
                                        }
                                    }
                                }
                                .padding(vertical = 8.dp, horizontal = 4.dp)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    if (searchHistory.isNotEmpty()) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(searchHistory) { historyItem ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            scope.launch {
                                                searchRepository.saveSearchQuery(historyItem.query).fold(
                                                    onSuccess = {
                                                        navController.navigate("search_result/${historyItem.query}")
                                                    },
                                                    onFailure = { error ->
                                                        Toast.makeText(context, error.message ?: "Lỗi khi lưu từ khóa", Toast.LENGTH_SHORT).show()
                                                    }
                                                )
                                            }
                                        }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = historyItem.query,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = {
                                            scope.launch {
                                                searchRepository.deleteSearchHistoryItem(historyItem.query).fold(
                                                    onSuccess = {
                                                        searchRepository.getSearchHistory().fold(
                                                            onSuccess = { (history, _) ->
                                                                searchHistory = history
                                                            },
                                                            onFailure = { error ->
                                                                Toast.makeText(context, error.message ?: "Lỗi khi tải lịch sử tìm kiếm", Toast.LENGTH_SHORT).show()
                                                            }
                                                        )
                                                    },
                                                    onFailure = { error ->
                                                        Toast.makeText(context, error.message ?: "Lỗi khi xóa từ khóa", Toast.LENGTH_SHORT).show()
                                                    }
                                                )
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Xóa",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    thickness = 0.5.dp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                                )
                            }
                            item {
                                Text(
                                    text = "Xóa tất cả",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            showClearAllDialog = true
                                        }
                                        .padding(vertical = 8.dp)
                                        .padding(horizontal = 12.dp)
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(400.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Chưa có lịch sử tìm kiếm",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            if (showClearAllDialog) {
                AlertDialog(
                    onDismissRequest = { showClearAllDialog = false },
                    title = { Text("Xác nhận") },
                    text = { Text("Bạn có chắc muốn xóa tất cả lịch sử tìm kiếm?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                scope.launch {
                                    searchRepository.clearSearchHistory().fold(
                                        onSuccess = {
                                            searchHistory = emptyList()
                                        },
                                        onFailure = { error ->
                                            Toast.makeText(context, error.message ?: "Lỗi khi xóa lịch sử tìm kiếm", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                    showClearAllDialog = false
                                }
                            }
                        ) {
                            Text("Xóa")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showClearAllDialog = false }) {
                            Text("Hủy")
                        }
                    }
                )
            }
        }
    }
}
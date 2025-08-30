package com.example.myapplication.ui.screens

import android.content.Intent
import android.net.Uri
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.myapplication.UiState
import com.example.myapplication.data.model.NewsArticle
import com.example.myapplication.data.model.NewsCategory
import com.example.myapplication.ui.components.TopBar
import com.example.myapplication.utils.ArticleSummarizer
import com.example.myapplication.viewmodel.NewsViewModel
import com.example.myapplication.viewmodel.UserViewModel
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun DetailScreen(
    articleId: String,
    onBack: () -> Unit,
    newsViewModel: NewsViewModel,
    userViewModel: UserViewModel = viewModel()
) {
    val articleState: State<NewsArticle?> = newsViewModel.getArticleState(articleId).collectAsState()
    val article = articleState.value
    val savedArticles by userViewModel.savedArticles.collectAsState()
    val readingHistory by userViewModel.readingHistory.collectAsState()
    val isCalculatingReliability by newsViewModel.isCalculatingReliability.collectAsState()
    var summaryState by remember { mutableStateOf<UiState>(UiState.Initial) }
    val scope = rememberCoroutineScope()
    val TAG = "DetailScreen"

    // Text-to-Speech setup
    val context = LocalContext.current
    var textToSpeech by remember { mutableStateOf<TextToSpeech?>(null) }
    var isSpeaking by remember { mutableStateOf(false) }
    var isTtsInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech?.setLanguage(Locale("vi", "VN"))
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "Ngôn ngữ tiếng Việt không được hỗ trợ trên thiết bị này")
                    Toast.makeText(context, "Ngôn ngữ tiếng Việt không được hỗ trợ", Toast.LENGTH_SHORT).show()
                } else {
                    isTtsInitialized = true
                    Log.d(TAG, "TextToSpeech khởi tạo thành công với tiếng Việt")
                }
                // Thiết lập listener để theo dõi trạng thái đọc
                textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        isSpeaking = true
                        Log.d(TAG, "Bắt đầu đọc văn bản")
                    }

                    override fun onDone(utteranceId: String?) {
                        isSpeaking = false
                        Log.d(TAG, "Hoàn tất đọc văn bản")
                    }

                    override fun onError(utteranceId: String?) {
                        isSpeaking = false
                        Log.e(TAG, "Lỗi khi đọc văn bản")
                        Toast.makeText(context, "Lỗi khi đọc bài viết", Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                Log.e(TAG, "Khởi tạo TextToSpeech thất bại: status=$status")
                Toast.makeText(context, "Không thể khởi tạo chức năng đọc", Toast.LENGTH_SHORT).show()
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
            Log.d(TAG, "TextToSpeech đã được giải phóng")
        }
    }

    LaunchedEffect(articleId) {
        Log.d(TAG, "Tải bài viết với ID: $articleId")
        newsViewModel.fetchArticleById(articleId)
        if (articleState.value == null) {
            Log.d(TAG, "Không tìm thấy bài viết trong Room, thử lấy từ Firestore")
            userViewModel.fetchReadingHistory()
            val firestoreArticle = readingHistory.find { it.id == articleId }
            if (firestoreArticle != null) {
                Log.d(TAG, "Tìm thấy bài viết trong Firestore: ${firestoreArticle.title}")
                newsViewModel.updateArticleState(articleId, firestoreArticle)
            } else {
                Log.d(TAG, "Không tìm thấy bài viết trong Firestore: $articleId")
            }
        }
    }

    LaunchedEffect(article) {
        if (article != null) {
            Log.d(TAG, "Thêm bài viết vào lịch sử đọc: ${article.title}")
            userViewModel.addToReadingHistory(article)
            if (article.reliabilityScore == null) {
                Log.d(TAG, "Bài viết chưa có reliabilityScore, bắt đầu tính toán")
                scope.launch {
                    newsViewModel.calculateReliabilityScore(article)
                }
            } else {
                Log.d(TAG, "Bài viết đã có reliabilityScore: ${article.reliabilityScore * 100}%")
            }
        }
    }

    val isSaved = savedArticles.any { it.id == articleId }

    Scaffold(
        topBar = {
            TopBar(
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Quay lại",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                },
                titleContent = {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "News AI",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = { /* Do nothing */ }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.surface, // Same as TopBar background
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (article != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TextButton(
                            onClick = {
                                if (isSaved) {
                                    userViewModel.removeSavedArticle(article.id)
                                } else {
                                    userViewModel.saveArticle(article)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = if (isSaved) "Bỏ lưu" else "Lưu bài viết"
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(if (isSaved) "Bỏ lưu" else "Lưu")
                        }
                        TextButton(
                            onClick = {
                                if (!isTtsInitialized) {
                                    Toast.makeText(context, "Chức năng đọc chưa sẵn sàng", Toast.LENGTH_SHORT).show()
                                    Log.w(TAG, "TTS chưa được khởi tạo, không thể đọc")
                                    return@TextButton
                                }
                                if (isSpeaking) {
                                    textToSpeech?.stop()
                                    isSpeaking = false
                                    Log.d(TAG, "Dừng đọc văn bản")
                                } else {
                                    val textToRead = "${article.title}. ${article.content ?: ""}"
                                    textToSpeech?.speak(
                                        textToRead,
                                        TextToSpeech.QUEUE_FLUSH,
                                        null,
                                        "article_$articleId"
                                    )
                                    isSpeaking = true
                                    Log.d(TAG, "Bắt đầu đọc bài viết: ${article.title}")
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isSpeaking) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = if (isSpeaking) "Dừng đọc" else "Đọc bài viết"
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(if (isSpeaking) "Dừng" else "Đọc")
                        }
                        article.articleUrl?.let { url ->
                            TextButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                                    context.startActivity(intent)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Chia sẻ"
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("Chia sẻ")
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (article != null) {
            val categoryDisplay = NewsCategory.joinCategories(article.category?.mapNotNull { NewsCategory.fromApiValue(it)?.displayName } ?: emptyList())
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
            ) {
                // Category
                Text(
                    text = categoryDisplay.ifEmpty { "Tin tức" },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Title
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 24.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Author and Published Date
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    article.author?.let { author ->
                        Text(
                            text = "Tác giả: $author",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } ?: Text(
                        text = "",
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = article.publishedAt ?: "Không có ngày",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Reliability Score
                if (isCalculatingReliability) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Đang tính toán độ tin cậy...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                } else {
                    Text(
                        text = "Độ tin cậy: ${"%.0f".format((article.reliabilityScore ?: 0f) * 100)}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if ((article.reliabilityScore ?: 0f) > 0.7f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Image
                article.imageUrl?.let { imageUrl ->
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Hình ảnh bài viết",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Summary Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Button(
                            onClick = {
                                scope.launch {
                                    summaryState = UiState.Loading
                                    summaryState = ArticleSummarizer.summarizeWithGemini(article.content)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(
                                text = "Tạo tóm tắt bằng AI",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        when (summaryState) {
                            is UiState.Initial -> {}
                            is UiState.Loading -> Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Đang tạo tóm tắt...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            is UiState.Success -> Text(
                                text = (summaryState as UiState.Success).outputText,
                                style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 28.sp),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(12.dp)
                            )
                            is UiState.Error -> Text(
                                text = "Lỗi: ${(summaryState as UiState.Error).errorMessage}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Content Section
                article.content?.let { content ->
                    Text(
                        text = content,
                        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 28.sp),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Justify
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = "Không tìm thấy bài viết",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
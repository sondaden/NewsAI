package com.example.myapplication.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.example.myapplication.data.model.NewsArticle
import com.example.myapplication.data.model.NewsCategory
import com.example.myapplication.viewmodel.UserViewModel

@Composable
fun NewsArticleItem(
    article: NewsArticle,
    onClick: () -> Unit,
    onDelete: (() -> Unit)? = null,
    userViewModel: UserViewModel? = null
) {
    val savedArticles by userViewModel?.savedArticles?.collectAsState() ?: return
    val isSaved = savedArticles.any { it.id == article.id }
    val scale by animateFloatAsState(targetValue = if (isSaved) 1.2f else 1.0f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Hiển thị ảnh bài viết
                article.imageUrl?.let { imageUrl ->
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Article Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Nội dung văn bản
                Column {
                    Text(
                        text = NewsCategory.joinCategories(article.category?.map { category ->
                            NewsCategory.fromApiValue(category)?.displayName ?: category
                        } ?: emptyList()),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = article.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (article.author != null) {
                            Text(
                                text = article.author,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                        Text(
                            text = article.publishedAt ?: "Không có ngày",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Nút bookmark kiểu ribbon ở góc trên bên phải
            if (userViewModel != null) {
                val backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                val iconColor = MaterialTheme.colorScheme.onPrimary
                val shape = RoundedCornerShape(bottomStart = 12.dp)

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(shape)
                        .background(backgroundColor)
                        .size(width = 40.dp, height = 44.dp)
                        .clickable {
                            if (isSaved) {
                                userViewModel.removeSavedArticle(article.id)
                            } else {
                                userViewModel.saveArticle(article)
                            }
                        }
                        .animateContentSize() // hiệu ứng thay đổi mượt
                        .scale(scale), // scale animation (sẵn có từ bạn)
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(targetState = isSaved, label = "Bookmark Transition") { saved ->
                        Icon(
                            imageVector = if (saved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = if (saved) "Bỏ lưu" else "Lưu bài viết",
                            tint = iconColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Nút xóa (nếu có)
            if (onDelete != null) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Xóa khỏi lịch sử",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
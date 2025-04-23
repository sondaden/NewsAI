package com.example.myapplication.ui.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.screens.AccountScreen
import com.example.myapplication.ui.screens.ChatbotScreen
import com.example.myapplication.ui.screens.DetailScreen
import com.example.myapplication.ui.screens.HomeScreen
import com.example.myapplication.ui.screens.LoginScreen
import com.example.myapplication.ui.screens.PodcastScreen
import com.example.myapplication.ui.screens.ReadingHistoryScreen
import com.example.myapplication.ui.screens.RegisterScreen
import com.example.myapplication.ui.screens.SavedArticlesScreen
import com.example.myapplication.ui.screens.VerifyEmailScreen
import com.example.myapplication.viewmodel.AuthUiState
import com.example.myapplication.viewmodel.AuthViewModel
import com.example.myapplication.viewmodel.ChatViewModel
import com.example.myapplication.viewmodel.NewsViewModel
import com.example.myapplication.viewmodel.UserViewModel
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Điều hướng chính của ứng dụng, quản lý các màn hình dựa trên trạng thái đăng nhập.
 * @param newsViewModel ViewModel quản lý dữ liệu tin tức.
 * @param authViewModel ViewModel quản lý xác thực người dùng.
 * @param userViewModel ViewModel quản lý hồ sơ và sở thích người dùng.
 * @param chatViewModel ViewModel quản lý logic chatbot.
 */
@Composable
fun AppNavigation(
    newsViewModel: NewsViewModel,
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel,
    chatViewModel: ChatViewModel // Thêm tham số chatViewModel
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val loginState by authViewModel.loginState.collectAsState()
    var isCheckingLogin by remember { mutableStateOf(true) }
    var isLoggedIn by remember { mutableStateOf(false) }
    val TAG = "AppNavigation"

    // Kiểm tra trạng thái đăng nhập khi khởi tạo
    LaunchedEffect(Unit) {
        authViewModel.checkLoginStatus(context)
        val result = withTimeoutOrNull(5000L) { // Timeout sau 5 giây
            while (loginState is AuthUiState.Initial || loginState is AuthUiState.Loading) {
                kotlinx.coroutines.delay(100)
            }
            loginState
        }
        result?.let { state ->
            isLoggedIn = state is AuthUiState.Success
            Log.d(TAG, "Login check completed: isLoggedIn = $isLoggedIn, state = $state")
        } ?: run {
            Log.w(TAG, "Login check timed out")
            isLoggedIn = false
        }
        isCheckingLogin = false
    }

    // Cập nhật trạng thái đăng nhập khi loginState thay đổi
    LaunchedEffect(loginState) {
        when (loginState) {
            is AuthUiState.Success -> {
                isLoggedIn = true
                navController.navigate("home") {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }
            is AuthUiState.Error -> isLoggedIn = false
            else -> Unit // Không làm gì với Initial hoặc Loading
        }
    }

    // Hiển thị NavHost khi đã kiểm tra xong trạng thái đăng nhập
    if (!isCheckingLogin) {
        NavHost(
            navController = navController,
            startDestination = if (isLoggedIn) "home" else "login"
        ) {
            composable("login") {
                LoginScreen(navController = navController, authViewModel = authViewModel)
            }
            composable("register") {
                RegisterScreen(navController = navController, authViewModel = authViewModel)
            }
            composable("verify_email") {
                VerifyEmailScreen(navController = navController, authViewModel = authViewModel)
            }
            composable("home") {
                HomeScreen(navController = navController, viewModel = newsViewModel)
            }
            composable("podcast") {
                PodcastScreen(navController = navController)
            }
            composable("chatbot") {
                ChatbotScreen(
                    navController = navController,
                    chatViewModel = chatViewModel // Truyền chatViewModel vào ChatbotScreen
                )
            }
            composable("saved") {
                SavedArticlesScreen(
                    navController = navController,
                    userViewModel = userViewModel
                )
            }
            composable("account") {
                AccountScreen(
                    navController = navController,
                    authViewModel = authViewModel,
                    userViewModel = userViewModel
                )
            }
            composable("reading_history") {
                ReadingHistoryScreen(
                    navController = navController,
                    userViewModel = userViewModel
                )
            }
            composable("detail/{articleId}") { backStackEntry ->
                val articleId = backStackEntry.arguments?.getString("articleId") ?: ""
                DetailScreen(
                    articleId = articleId,
                    navController = navController,
                    newsViewModel = newsViewModel
                )
            }
        }
    }
}
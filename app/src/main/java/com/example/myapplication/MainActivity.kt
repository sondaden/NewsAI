package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.myapplication.ui.screens.*
import com.example.myapplication.ui.theme.NewsAppTheme
import androidx.activity.viewModels
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.viewmodel.NewsViewModel
import com.example.myapplication.repository.NewsRepository
import com.example.myapplication.viewmodel.AuthViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull

class MainActivity : ComponentActivity() {
  private val newsRepository = NewsRepository()
  private val newsViewModel: NewsViewModel by viewModels { NewsViewModelFactory(newsRepository) }
  private val authViewModel: AuthViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    FirebaseApp.initializeApp(this)
    Log.d("Firebase", "Firebase đã được kết nối thành công!")

    setContent {
      Log.d("DEBUG", "Giao diện đang được render...")
      val context = LocalContext.current
      val loginState by authViewModel.loginState.observeAsState()
      val isCheckingLogin = remember { mutableStateOf(true) }
      val isLoggedIn = remember { mutableStateOf(false) }

      // Kiểm tra trạng thái đăng nhập
      LaunchedEffect(Unit) {
        try {
          authViewModel.checkLoginStatus(context)
          // Add timeout to prevent infinite white screen
          withTimeoutOrNull(5000L) {
            while (loginState == null) {
              delay(100)
            }
          }
        } catch (e: Exception) {
          Log.e("MainActivity", "Error checking login status: ${e.message}")
        } finally {
          // Always update this flag even if there's an error
          isCheckingLogin.value = false
        }
      }

      // Cập nhật giao diện theo trạng thái đăng nhập
      LaunchedEffect(loginState) {
        loginState?.let { result ->
          isLoggedIn.value = result.isSuccess
          isCheckingLogin.value = false
        }
      }

      if (!isCheckingLogin.value) {
        NewsApp(newsViewModel, isLoggedIn.value)
      }
    }
  }
}


@Composable
fun NewsApp(viewModel: NewsViewModel, isLoggedIn: Boolean) {
  val navController = rememberNavController()

  NavHost(navController = navController, startDestination = if (isLoggedIn) "home" else "login") {
    composable("login") { LoginScreen(navController) }
    composable("register") { RegisterScreen(navController) }
    composable("verify_email") { VerifyEmailScreen(navController) }
    composable("home") { HomeScreen(navController, viewModel) }
    composable("podcast") { PodcastScreen(navController) }
    composable("chatbot") { ChatbotScreen(navController) }
    composable("saved") { SavedArticlesScreen(navController) }
    composable("account") { AccountScreen(navController) }
    composable("reading_history") { ReadingHistoryScreen(navController) }
    composable("detail/{articleId}") { backStackEntry ->
      val articleId = backStackEntry.arguments?.getString("articleId") ?: ""
      DetailScreen(articleId = articleId, onBack = { navController.popBackStack() })
    }
  }
}

class NewsViewModelFactory(private val repository: NewsRepository) : ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(NewsViewModel::class.java)) {
      @Suppress("UNCHECKED_CAST")
      return NewsViewModel(repository) as T
    }
    throw IllegalArgumentException("Unknown ViewModel class")
  }
}

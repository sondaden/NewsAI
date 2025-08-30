package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.ai.GeminiService
import com.example.myapplication.data.firestore.FirestoreService
import com.example.myapplication.repository.NewsRepository
import com.example.myapplication.ui.screens.*
import com.example.myapplication.ui.theme.NewsAppTheme
import com.example.myapplication.viewmodel.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull

// Định nghĩa các route bằng sealed class
sealed class AppRoute(val route: String) {
  object Login : AppRoute("login")
  object Register : AppRoute("register")
  object VerifyEmail : AppRoute("verify_email")
  object ForgotPassword : AppRoute("forgot_password")
  object Home : AppRoute("home")
  object Search : AppRoute("search")
  object SearchResult : AppRoute("search_result/{query}")
  object Chatbot : AppRoute("chatbot")
  object Saved : AppRoute("saved")
  object Account : AppRoute("account")
  object ReadingHistory : AppRoute("reading_history")
  object PreferenceManagement : AppRoute("preference_management")
  object AccountManagement : AppRoute("account_management")
  object Podcast : AppRoute("podcast")
  object Detail : AppRoute("detail/{articleId}")
}

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      val context = LocalContext.current
      val userViewModel: UserViewModel = viewModel(factory = UserViewModelFactory(context))
      val newsViewModel: NewsViewModel = viewModel(factory = NewsViewModelFactory(context))
      val chatViewModel: ChatViewModel = viewModel(factory = ChatViewModelFactory(context))
      val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory())
      val preferenceViewModel: PreferenceViewModel = viewModel(factory = PreferenceViewModelFactory(context))
      val isDarkTheme by userViewModel.isDarkTheme.collectAsState()

      NewsAppTheme(darkTheme = isDarkTheme) {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          val loginState by authViewModel.loginState.observeAsState()
          val isCheckingLogin = remember { mutableStateOf(true) }
          val isLoggedIn = remember { mutableStateOf(false) }

          LaunchedEffect(Unit) {
            try {
              authViewModel.checkLoginStatus(context)
              withTimeoutOrNull(1000L) {
                while (loginState == null) {
                  delay(100)
                }
              }
            } catch (e: Exception) {
              Log.e("MainActivity", "Lỗi khi kiểm tra trạng thái đăng nhập: ${e.message}")
            } finally {
              isCheckingLogin.value = false
            }
          }

          LaunchedEffect(loginState) {
            loginState?.let { result ->
              isLoggedIn.value = result.isSuccess
              isCheckingLogin.value = false
            }
          }

          if (!isCheckingLogin.value) {
            NewsApp(
              newsViewModel = newsViewModel,
              chatViewModel = chatViewModel,
              userViewModel = userViewModel,
              authViewModel = authViewModel,
              preferenceViewModel = preferenceViewModel,
              isLoggedIn = isLoggedIn.value
            )
          }
        }
      }
    }
  }
}

@Composable
fun NewsApp(
  newsViewModel: NewsViewModel,
  chatViewModel: ChatViewModel,
  userViewModel: UserViewModel,
  authViewModel: AuthViewModel,
  preferenceViewModel: PreferenceViewModel,
  isLoggedIn: Boolean
) {
  val navController = rememberNavController()

  NavHost(
    navController = navController,
    startDestination = if (isLoggedIn) AppRoute.Home.route else AppRoute.Login.route
  ) {
    composable(
      AppRoute.Login.route,
      enterTransition = {
        slideInHorizontally(initialOffsetX = { -1000 }) + fadeIn(animationSpec = tween(300))
      },
      exitTransition = {
        slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut(animationSpec = tween(300))
      },
      popEnterTransition = {
        slideInHorizontally(initialOffsetX = { -1000 }) + fadeIn(animationSpec = tween(300))
      },
      popExitTransition = {
        slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut(animationSpec = tween(300))
      }
    ) {
      LoginScreen(navController, authViewModel)
    }
    composable(
      AppRoute.Register.route,
      enterTransition = {
        slideInHorizontally(initialOffsetX = { 1000 }) + fadeIn(animationSpec = tween(300))
      },
      exitTransition = {
        slideOutHorizontally(targetOffsetX = { 1000 }) + fadeOut(animationSpec = tween(300))
      },
      popEnterTransition = {
        slideInHorizontally(initialOffsetX = { -1000 }) + fadeIn(animationSpec = tween(300))
      },
      popExitTransition = {
        slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut(animationSpec = tween(300))
      }
    ) {
      RegisterScreen(navController, authViewModel, userViewModel)
    }
    composable(
      AppRoute.VerifyEmail.route,
      enterTransition = {
        slideInHorizontally(initialOffsetX = { 1000 }) + fadeIn(animationSpec = tween(300))
      },
      exitTransition = {
        slideOutHorizontally(targetOffsetX = { 1000 }) + fadeOut(animationSpec = tween(300))
      },
      popEnterTransition = {
        slideInHorizontally(initialOffsetX = { -1000 }) + fadeIn(animationSpec = tween(300))
      },
      popExitTransition = {
        slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut(animationSpec = tween(300))
      }
    ) {
      VerifyEmailScreen(navController, authViewModel, userViewModel)
    }
    composable(
      AppRoute.ForgotPassword.route,
      enterTransition = {
        slideInHorizontally(initialOffsetX = { 1000 }) + fadeIn(animationSpec = tween(300))
      },
      exitTransition = {
        slideOutHorizontally(targetOffsetX = { 1000 }) + fadeOut(animationSpec = tween(300))
      },
      popEnterTransition = {
        slideInHorizontally(initialOffsetX = { -1000 }) + fadeIn(animationSpec = tween(300))
      },
      popExitTransition = {
        slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut(animationSpec = tween(300))
      }
    ) {
      ForgotPasswordScreen(navController, authViewModel)
    }
    composable(
      AppRoute.Home.route,
      enterTransition = {
        slideInHorizontally(initialOffsetX = { 1000 }) + fadeIn(animationSpec = tween(300))
      },
      exitTransition = {
        slideOutHorizontally(targetOffsetX = { 1000 }) + fadeOut(animationSpec = tween(300))
      },
      popEnterTransition = {
        slideInHorizontally(initialOffsetX = { -1000 }) + fadeIn(animationSpec = tween(300))
      },
      popExitTransition = {
        slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut(animationSpec = tween(300))
      }
    ) {
      HomeScreen(navController, newsViewModel, userViewModel)
    }
    composable(
      AppRoute.Search.route,
      enterTransition = {
        slideInHorizontally(initialOffsetX = { 1000 }) + fadeIn(animationSpec = tween(300))
      },
      exitTransition = {
        slideOutHorizontally(targetOffsetX = { 1000 }) + fadeOut(animationSpec = tween(300))
      },
      popEnterTransition = {
        slideInHorizontally(initialOffsetX = { -1000 }) + fadeIn(animationSpec = tween(300))
      },
      popExitTransition = {
        slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut(animationSpec = tween(300))
      }
    ) {
      SearchScreen(navController, newsViewModel)
    }
    composable(
      route = AppRoute.SearchResult.route,
      arguments = listOf(navArgument("query") { type = NavType.StringType }),
      enterTransition = {
        slideInHorizontally(initialOffsetX = { 1000 }) + fadeIn(animationSpec = tween(300))
      },
      exitTransition = {
        slideOutHorizontally(targetOffsetX = { 1000 }) + fadeOut(animationSpec = tween(300))
      },
      popEnterTransition = {
        slideInHorizontally(initialOffsetX = { -1000 }) + fadeIn(animationSpec = tween(300))
      },
      popExitTransition = {
        slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut(animationSpec = tween(300))
      }
    ) { backStackEntry ->
      val query = backStackEntry.arguments?.getString("query") ?: ""
      SearchResultScreen(
        navController = navController,
        newsViewModel = newsViewModel,
        userViewModel = userViewModel,
        initialQuery = query
      )
    }
    composable(
      AppRoute.Chatbot.route,
      enterTransition = {
        slideInHorizontally(initialOffsetX = { 1000 }) + fadeIn(animationSpec = tween(300))
      },
      exitTransition = {
        slideOutHorizontally(targetOffsetX = { 1000 }) + fadeOut(animationSpec = tween(300))
      },
      popEnterTransition = {
        slideInHorizontally(initialOffsetX = { -1000 }) + fadeIn(animationSpec = tween(300))
      },
      popExitTransition = {
        slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut(animationSpec = tween(300))
      }
    ) {
      ChatbotScreen(navController)
    }
    composable(
      AppRoute.Saved.route,
      enterTransition = {
        slideInHorizontally(initialOffsetX = { 1000 }) + fadeIn(animationSpec = tween(300))
      },
      exitTransition = {
        slideOutHorizontally(targetOffsetX = { 1000 }) + fadeOut(animationSpec = tween(300))
      },
      popEnterTransition = {
        slideInHorizontally(initialOffsetX = { -1000 }) + fadeIn(animationSpec = tween(300))
      },
      popExitTransition = {
        slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut(animationSpec = tween(300))
      }
    ) {
      SavedArticlesScreen(navController, userViewModel)
    }
    composable(
      AppRoute.Account.route,
      enterTransition = {
        slideInHorizontally(initialOffsetX = { 1000 }) + fadeIn(animationSpec = tween(300))
      },
      exitTransition = {
        slideOutHorizontally(targetOffsetX = { 1000 }) + fadeOut(animationSpec = tween(300))
      },
      popEnterTransition = {
        slideInHorizontally(initialOffsetX = { -1000 }) + fadeIn(animationSpec = tween(300))
      },
      popExitTransition = {
        slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut(animationSpec = tween(300))
      }
    ) {
      AccountScreen(navController, authViewModel, userViewModel)
    }
    composable(
      AppRoute.ReadingHistory.route,
      enterTransition = {
        slideInHorizontally(initialOffsetX = { 1000 }) + fadeIn(animationSpec = tween(300))
      },
      exitTransition = {
        slideOutHorizontally(targetOffsetX = { 1000 }) + fadeOut(animationSpec = tween(300))
      },
      popEnterTransition = {
        slideInHorizontally(initialOffsetX = { -1000 }) + fadeIn(animationSpec = tween(300))
      },
      popExitTransition = {
        slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut(animationSpec = tween(300))
      }
    ) {
      ReadingHistoryScreen(navController, userViewModel)
    }
    composable(
      AppRoute.PreferenceManagement.route,
      enterTransition = {
        slideInHorizontally(initialOffsetX = { 1000 }) + fadeIn(animationSpec = tween(300))
      },
      exitTransition = {
        slideOutHorizontally(targetOffsetX = { 1000 }) + fadeOut(animationSpec = tween(300))
      },
      popEnterTransition = {
        slideInHorizontally(initialOffsetX = { -1000 }) + fadeIn(animationSpec = tween(300))
      },
      popExitTransition = {
        slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut(animationSpec = tween(300))
      }
    ) {
      PreferenceManagementScreen(navController = navController, preferenceViewModel = preferenceViewModel)
    }
    composable(
      AppRoute.AccountManagement.route,
      enterTransition = {
        slideInHorizontally(initialOffsetX = { 1000 }) + fadeIn(animationSpec = tween(300))
      },
      exitTransition = {
        slideOutHorizontally(targetOffsetX = { 1000 }) + fadeOut(animationSpec = tween(300))
      },
      popEnterTransition = {
        slideInHorizontally(initialOffsetX = { -1000 }) + fadeIn(animationSpec = tween(300))
      },
      popExitTransition = {
        slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut(animationSpec = tween(300))
      }
    ) {
      AccountManagementScreen(navController)
    }
    composable(
      AppRoute.Podcast.route,
      enterTransition = {
        slideInHorizontally(initialOffsetX = { 1000 }) + fadeIn(animationSpec = tween(300))
      },
      exitTransition = {
        slideOutHorizontally(targetOffsetX = { 1000 }) + fadeOut(animationSpec = tween(300))
      },
      popEnterTransition = {
        slideInHorizontally(initialOffsetX = { -1000 }) + fadeIn(animationSpec = tween(300))
      },
      popExitTransition = {
        slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut(animationSpec = tween(300))
      }
    ) {
      PodcastScreen(navController)
    }
    composable(
      route = AppRoute.Detail.route,
      arguments = listOf(navArgument("articleId") { type = NavType.StringType }),
      enterTransition = {
        slideInHorizontally(initialOffsetX = { 1000 }) + fadeIn(animationSpec = tween(300))
      },
      exitTransition = {
        slideOutHorizontally(targetOffsetX = { 1000 }) + fadeOut(animationSpec = tween(300))
      },
      popEnterTransition = {
        slideInHorizontally(initialOffsetX = { -1000 }) + fadeIn(animationSpec = tween(300))
      },
      popExitTransition = {
        slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut(animationSpec = tween(300))
      }
    ) { backStackEntry ->
      val articleId = backStackEntry.arguments?.getString("articleId") ?: ""
      DetailScreen(
        articleId = articleId,
        onBack = { navController.popBackStack() },
        newsViewModel = newsViewModel,
        userViewModel = userViewModel
      )
    }
  }
}

class UserViewModelFactory(private val context: android.content.Context) : ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
      @Suppress("UNCHECKED_CAST")
      return UserViewModel(context) as T
    }
    throw IllegalArgumentException("Unknown ViewModel class")
  }
}

class NewsViewModelFactory(private val context: android.content.Context) : ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(NewsViewModel::class.java)) {
      @Suppress("UNCHECKED_CAST")
      return NewsViewModel(NewsRepository(context)) as T
    }
    throw IllegalArgumentException("Unknown ViewModel class")
  }
}

class ChatViewModelFactory(private val context: android.content.Context) : ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
      @Suppress("UNCHECKED_CAST")
      return ChatViewModel(
        newsRepository = NewsRepository(context),
        geminiService = GeminiService(),
        context = context
      ) as T
    }
    throw IllegalArgumentException("Unknown ViewModel class")
  }
}

class AuthViewModelFactory : ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
      @Suppress("UNCHECKED_CAST")
      return AuthViewModel() as T
    }
    throw IllegalArgumentException("Unknown ViewModel class")
  }
}

class PreferenceViewModelFactory(private val context: android.content.Context) : ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(PreferenceViewModel::class.java)) {
      @Suppress("UNCHECKED_CAST")
      return PreferenceViewModel(FirestoreService(NewsRepository(context))) as T
    }
    throw IllegalArgumentException("Unknown ViewModel class")
  }
}
package com.infomanix.getpyq.ui.navigation

import android.annotation.SuppressLint
import android.os.Environment
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.infomanix.getpyq.ui.screen.CameraScannerScreen
import com.infomanix.getpyq.ui.screen.Home
import com.infomanix.getpyq.ui.screen.EasterScreen
import com.infomanix.getpyq.ui.screen.EditImageScreen
import com.infomanix.getpyq.ui.screen.FolderListScreen
import com.infomanix.getpyq.ui.screen.GridPreviewScreen
import com.infomanix.getpyq.ui.screen.SubjectListScreen
import com.infomanix.getpyq.ui.viewmodels.FileViewModel
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.infomanix.getpyq.data.UserPreferences
import com.infomanix.getpyq.ui.screen.PdfListScreen
import com.infomanix.getpyq.ui.screen.PdfViewerScreen
import com.infomanix.getpyq.ui.screen.SplashScreen
import com.infomanix.getpyq.ui.screen.TestScreen
import com.infomanix.getpyq.ui.screen.UploaderLoginScreen
import com.infomanix.getpyq.ui.screen.UploaderSignupScreen
import com.infomanix.getpyq.ui.viewmodels.UserViewModel
import com.infomanix.getpyq.utils.AuthManagerUtils

fun getAppPicturesPath(context: Context): String {
    return context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath ?: ""
}

@SuppressLint("NewApi")
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavigation(navController: NavHostController) {
    
    val fileViewModel: FileViewModel = viewModel()
    val userViewModel: UserViewModel = viewModel()
    val rootPath = getAppPicturesPath(navController.context)
    val context = LocalContext.current
    // ✅ Fetch the UserPreferences instance just once
    val userPreferences = remember { UserPreferences.getInstance(context) }
    val isLoggedIn = remember { AuthManagerUtils.isLoggedIn() }
    LaunchedEffect(Unit) {
        userViewModel.loadUserState(context)
    }
    // ✅ Collect the splash state properly
    val hasSeenSplash by userPreferences.hasSeenSplash.collectAsStateWithLifecycle(initialValue = false)

    NavHost(
        navController = navController,
        startDestination = if (hasSeenSplash) "home" else "splash"
        //startDestination = "test"
    ) {
        // ✅ Splash screen for first-time setup
        composable(
            "splash",
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }) {
            SplashScreen(navController, context = context)
        }
        // ✅ Home screen (No Animation)
        composable(
            "home",
            enterTransition = { slideInBottom() },
            exitTransition = { slideOutTop() }) { Home() }

        composable("test", enterTransition = { slideInBottom() }, exitTransition = { slideOutTop() }) { TestScreen() }
        // ✅ Camera Screen (Slide + Fade)
        composable(
            "camera",
            enterTransition = { slideInBottom() },
            exitTransition = { slideOutTop() }) {
            CameraScannerScreen(navController, fileViewModel)
        }

        // ✅ Subject List Screen (Slide Animation)
        composable(
            "subjects/{branch}/{semester}/{mode}",
            arguments = listOf(
                navArgument("semester") { type = NavType.StringType },
                navArgument("branch") { type = NavType.StringType },
                navArgument("mode") { type = NavType.StringType }
            ),
            enterTransition = { slideInRight() }, exitTransition = { slideOutLeft() }
        ) { backStackEntry ->
            val semester = backStackEntry.arguments?.getString("semester") ?: "8"
            val branch = backStackEntry.arguments?.getString("branch") ?: "EE"
            val isMidsem =
                backStackEntry.arguments?.getString("mode")?.toBooleanStrictOrNull() ?: false
            SubjectListScreen()
        }
        composable("pdfList/{semester}/{subject}/{examType}") { backStackEntry ->
            val semester = backStackEntry.arguments?.getString("semester") ?: "1"
            val subject = backStackEntry.arguments?.getString("subject") ?: "EE101"
            val regex = Regex("""([A-Z]{2}\s?\d{3})""") // Matches codes like "CS 209" or "CS209"
            val subjectcode = regex.find(subject)?.value?.replace(" ", "") ?: "MK101"
            val examType = backStackEntry.arguments?.getString("examType") ?: "midsem"
            PdfListScreen(navController = navController,semester = semester, subjectcode = subjectcode, examType =  examType)
        }
        // ✅ Edit Screen (Pop + Slide)
        composable("edit",
            enterTransition = { slideInRight() }, exitTransition = { slideOutLeft() }
        ) { backStackEntry ->
            val index = backStackEntry.arguments?.getString("index")?.toIntOrNull() ?: 0
            EditImageScreen(navController, fileViewModel)
        }

        // ✅ Grid Preview Screen (Slide In)
        composable(
            "gridPreview",
            enterTransition = { slideInLeft() },
            exitTransition = { slideOutRight() }) {
            GridPreviewScreen(navController, fileViewModel)
        }
        composable(
            "myL7V3",
            enterTransition = { slideInLeft() },
            exitTransition = { slideOutRight() }) {
            EasterScreen(navController)
        }
        composable(
            "pdfs",
            enterTransition = { slideInLeft() },
            exitTransition = { slideOutRight() }) {
            FolderListScreen(navController, fileViewModel, userViewModel, rootPath)
        }
        composable("pdfViewer/{pdfUrl}") { backStackEntry ->
            val pdfUrl = backStackEntry.arguments?.getString("pdfUrl") ?: ""
            PdfViewerScreen(navController, Uri.decode(pdfUrl)) // ✅ Decode safe URL
        }
        composable(
            "login",
            enterTransition = { slideInLeft() },
            exitTransition = { slideOutRight() }) {
            UploaderLoginScreen(navController, userViewModel)
        }

        // ✅ Signup Screen (Optional)
        composable("signup", enterTransition = { slideInLeft() }, exitTransition = { slideOutRight() }) {
            UploaderSignupScreen(navController, userViewModel)
        }
    }
}

fun slideInRight() = slideInHorizontally(initialOffsetX = { it }) + fadeIn()

fun slideOutLeft() = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut()

fun slideInLeft() = slideInHorizontally(initialOffsetX = { -it }) + fadeIn()

fun slideOutRight() = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()

fun slideInBottom() = slideInVertically(initialOffsetY = { it }) + fadeIn()

fun slideOutTop() = slideOutVertically(targetOffsetY = { -it }) + fadeOut()

fun popIn() = scaleIn(initialScale = 0.9f) + fadeIn()

fun popOut() = scaleOut(targetScale = 1.1f) + fadeOut()

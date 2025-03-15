package com.infomanix.getpyq.ui.navigation

import android.os.Environment
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.*
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
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

fun getAppPicturesPath(context: Context): String {
    return context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath ?: ""
}
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavigation(navController: NavHostController) {
    val navController = rememberAnimatedNavController()
    val fileViewModel: FileViewModel = viewModel()
    val rootPath = getAppPicturesPath(navController.context)

    AnimatedNavHost(
        navController = navController,
        startDestination = "home"
    ) {
        // ✅ Home screen (No Animation)
        composable("home", enterTransition = { slideInBottom() }, exitTransition = { slideOutTop() }) { Home(navController) }

        // ✅ Camera Screen (Slide + Fade)
        composable("camera", enterTransition = { slideInBottom() }, exitTransition = { slideOutTop() }) {
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
            val isMidsem = backStackEntry.arguments?.getString("mode")?.toBooleanStrictOrNull() ?: false
            SubjectListScreen(navController, semester, branch, isMidsem)
        }

        // ✅ Edit Screen (Pop + Slide)
        composable("edit",
            enterTransition = { slideInRight() }, exitTransition = { slideOutLeft() }
        ) { backStackEntry ->
            val index = backStackEntry.arguments?.getString("index")?.toIntOrNull() ?: 0
            EditImageScreen(navController, fileViewModel)
        }

        // ✅ Grid Preview Screen (Slide In)
        composable("gridPreview", enterTransition = { slideInLeft() }, exitTransition = { slideOutRight() }) {
            GridPreviewScreen(navController, fileViewModel)
        }
        composable("myL7V3", enterTransition = { slideInLeft() }, exitTransition = { slideOutRight() }) {
            EasterScreen(navController)
        }
        composable("pdfs", enterTransition = { slideInLeft() }, exitTransition = { slideOutRight() }) {
            FolderListScreen(navController,fileViewModel,rootPath)
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

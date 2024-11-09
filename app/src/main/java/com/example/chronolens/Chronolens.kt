package com.example.chronolens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.chronolens.ui.components.ChronolensBottomBar
import com.example.chronolens.ui.components.ChronolensTopAppBar
import com.example.chronolens.ui.screens.FullscreenMediaView
import com.example.chronolens.ui.screens.LoginScreen
import com.example.chronolens.ui.screens.MediaGridScreen
import com.example.chronolens.ui.theme.ChronoLensTheme
import com.example.chronolens.viewModels.MediaGridScreenViewModel
import com.example.chronolens.viewModels.UserViewModel
import com.example.chronolens.viewModels.ViewModelProvider
import com.example.chronolens.viewModels.WorkManagerViewModel

enum class ChronolensNav {
    Login,
    MediaGrid,
    FullScreenMedia,
    Albums,
    Settings,
    Search,
    Error
}

@Composable
fun ChronoLens() {

    ChronoLensTheme {
        val navController = rememberNavController()

        val userViewModel: UserViewModel = viewModel(factory = ViewModelProvider.Factory)
        val userState = userViewModel.userState.collectAsState()
        val mediaGridScreenViewModel: MediaGridScreenViewModel =
            viewModel(factory = ViewModelProvider.Factory)
        val mediaGridScreenState = mediaGridScreenViewModel.mediaGridState.collectAsState()
        val fullscreenMediaState =
            mediaGridScreenViewModel.fullscreenImageState.collectAsState()
        val workManagerViewModel: WorkManagerViewModel =
            viewModel(factory = ViewModelProvider.Factory)

        val backStackEntry by navController.currentBackStackEntryAsState()

        val currentRoute = backStackEntry?.destination?.route ?: ChronolensNav.Error.name
        val currentScreen = ChronolensNav.valueOf(currentRoute)

        val navigationBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

//        Scaffold(
//            topBar = {
//                ChronolensTopAppBar(
//                    canNavigateBack = navController.previousBackStackEntry != null,
//                    navigateUp = { navController.navigateUp() },
//                    currentScreen = currentScreen,
//                    userLoginState = userState.value.userLoginState
//                )
//            },
//            bottomBar = {
//                ChronolensBottomBar(
//                    currentScreen = currentScreen,
//                    nav = navController
//                )
//            }
//        )
        val theme = MaterialTheme.colorScheme

        val gradientBrush = Brush.linearGradient(
            colors = listOf(theme.primary, theme.secondary),
            start = Offset(0f, 0f),
            end = Offset(1080f, 0f)
        )
        val commonModifier = Modifier
            .background(gradientBrush)
            .padding(bottom = navigationBarPadding)

        NavHost(
            navController = navController,
            startDestination = ChronolensNav.Login.name
        ) {
            composable(ChronolensNav.MediaGrid.name) {
                MediaGridScreen(
                    viewModel = mediaGridScreenViewModel,
                    state = mediaGridScreenState,
                    navController = navController,
                    work = workManagerViewModel,
                    modifier = commonModifier
                )
            }
            composable(ChronolensNav.FullScreenMedia.name) {
                FullscreenMediaView(
                    viewModel = mediaGridScreenViewModel,
                    mediaGridState = mediaGridScreenState,
                    fullscreenMediaState = fullscreenMediaState,
                    navController = navController,
                    modifier = commonModifier
                )
            }
            composable(ChronolensNav.Login.name) {
                LoginScreen(
                    viewModel = userViewModel,
                    userState = userState,
                    grantAccess = {
                        mediaGridScreenViewModel.init()
                        navController.navigate(ChronolensNav.MediaGrid.name)
                    },
                    modifier = commonModifier
                )
            }
        }
    }
}
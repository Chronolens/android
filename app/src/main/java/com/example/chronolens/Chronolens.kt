package com.example.chronolens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.chronolens.ui.components.ChronolensBottomBar
import com.example.chronolens.ui.components.ChronolensTopAppBar
import com.example.chronolens.ui.screens.AlbumsScreen
import com.example.chronolens.ui.screens.settings.BackgroundUploadScreen
import com.example.chronolens.ui.screens.FullscreenMediaView
import com.example.chronolens.ui.screens.LoginScreen
import com.example.chronolens.ui.screens.MediaGridScreen
import com.example.chronolens.ui.screens.SearchScreen
import com.example.chronolens.ui.screens.SettingsScreen
import com.example.chronolens.ui.screens.settings.ActivityHistoryScreen
import com.example.chronolens.ui.screens.settings.MachineLearningScreen
import com.example.chronolens.ui.theme.ChronoLensTheme
import com.example.chronolens.utils.ChronolensNav
import com.example.chronolens.viewModels.MediaGridScreenViewModel
import com.example.chronolens.viewModels.UserViewModel
import com.example.chronolens.viewModels.ViewModelProvider
import com.example.chronolens.viewModels.WorkManagerViewModel

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
        val workManagerState = workManagerViewModel.workManagerState.collectAsState()


        val backStackEntry by navController.currentBackStackEntryAsState()

        val currentRoute = backStackEntry?.destination?.route ?: ChronolensNav.Login.name
        val currentScreen = ChronolensNav.valueOf(currentRoute)

        val navigationBarPadding =
            WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

        Scaffold(
            topBar = {
                ChronolensTopAppBar(
                    canNavigateBack = navController.previousBackStackEntry != null,
                    navigateUp = { navController.navigateUp() },
                    currentScreen = currentScreen,
                    userLoginState = userState.value.userLoginState
                )
            },
            bottomBar = {
                ChronolensBottomBar(
                    currentScreen = currentScreen,
                    nav = navController,
                    navigationBarPadding = navigationBarPadding
                )
            }
        )
        { innerPadding ->
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
                        modifier = Modifier
                            .padding(innerPadding),
                        refreshPaddingValues = innerPadding.calculateTopPadding() - 20.dp
                    )
                }

                composable(ChronolensNav.FullScreenMedia.name) {
                    FullscreenMediaView(
                        viewModel = mediaGridScreenViewModel,
                        mediaGridState = mediaGridScreenState,
                        fullscreenMediaState = fullscreenMediaState,
                        navController = navController,
                        modifier = Modifier
                            .padding(innerPadding)
                    )
                }

                composable(ChronolensNav.Login.name) {
                    LoginScreen(
                        viewModel = userViewModel,
                        userState = userState,
                        grantAccess = {
                            mediaGridScreenViewModel.init()
                            navController.navigate(ChronolensNav.MediaGrid.name) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        modifier = Modifier
                            .padding(bottom = navigationBarPadding)
                            .padding(innerPadding)
                    )
                }

                composable(ChronolensNav.Albums.name) {
                    AlbumsScreen(
                        viewModel = mediaGridScreenViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }

                composable(ChronolensNav.Search.name) {
                    SearchScreen(modifier = Modifier.padding(innerPadding))
                }

                composable(ChronolensNav.Settings.name) {
                    SettingsScreen(
                        modifier = Modifier.padding(innerPadding),
                        state = userState,
                        navController = navController
                    )
                }

                composable(ChronolensNav.Error.name) {
                    Box(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxWidth()
                    ) {
                        Text("Coming soon", color = Color.White)
                    }
                }

                composable(ChronolensNav.BackgroundUpload.name) {
                    BackgroundUploadScreen(
                        modifier = Modifier.padding(innerPadding),
                        workManager = workManagerViewModel,
                        workManagerState = workManagerState
                    )
                }

                composable(ChronolensNav.ActivityHistory.name) {
                    ActivityHistoryScreen(modifier = Modifier.padding(innerPadding))
                }

                composable(ChronolensNav.MachineLearning.name) {
                    MachineLearningScreen(modifier = Modifier.padding(innerPadding))
                }

            }
        }
    }
}
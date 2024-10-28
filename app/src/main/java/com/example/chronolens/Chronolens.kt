package com.example.chronolens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chronolens.ui.screens.FullscreenMediaView
import com.example.chronolens.ui.screens.LoginScreen
import com.example.chronolens.ui.screens.MediaGridScreen
import com.example.chronolens.ui.theme.ChronoLensTheme
import com.example.chronolens.viewModels.MediaGridScreenViewModel
import com.example.chronolens.viewModels.UserLoginState
import com.example.chronolens.viewModels.UserViewModel
import com.example.chronolens.viewModels.ViewModelProvider

@Composable
fun ChronoLens() {

    ChronoLensTheme {
        val navController = rememberNavController() // NavController instance

        val userViewModel: UserViewModel = viewModel(factory = ViewModelProvider.Factory)
        val userState = userViewModel.userState.collectAsState()
        val mediaGridScreenViewModel: MediaGridScreenViewModel =
            viewModel(factory = ViewModelProvider.Factory)
        val mediaGridScreenState = mediaGridScreenViewModel.mediaGridState.collectAsState()
        val fullscreenMediaState =
            mediaGridScreenViewModel.fullscreenImageState.collectAsState()


        NavHost(
            navController = navController,
            startDestination = "loginPage" // The starting route for navigation
        ) {
            // Define the MediaGridScreen route
            composable("mediaGrid") {
                MediaGridScreen(
                    viewModel = mediaGridScreenViewModel,
                    state = mediaGridScreenState,
                    navController = navController
                )
            }
            // Define the FullscreenMediaView route
            composable("fullscreenMediaView") {
                FullscreenMediaView(
                    viewModel = mediaGridScreenViewModel,
                    mediaGridState = mediaGridScreenState,
                    fullscreenMediaState = fullscreenMediaState,
                    navController = navController,
                )
            }
            composable("loginPage") {
                LoginScreen(
                    viewModel = userViewModel,
                    userState = userState,
                    grantAccess = {
                        mediaGridScreenViewModel.init()
                        navController.navigate("mediaGrid")
                    }
                )
            }

        }
    }
}
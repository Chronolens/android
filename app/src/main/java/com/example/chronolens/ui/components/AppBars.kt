package com.example.chronolens.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.chronolens.utils.ChronolensNav
import com.example.chronolens.utils.noBottomBar
import com.example.chronolens.utils.noTopBar
import com.example.chronolens.viewModels.UserLoginState

//TODO: beautify here
@Composable
fun ChronolensBottomBar(
    currentScreen: ChronolensNav,
    nav: NavHostController
) {
    val modifier = Modifier.scale(1.2f)

    if (!noBottomBar.contains(currentScreen)) {
        BottomAppBar(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.weight(0.5f))

                // Home Button
                Button(
                    onClick = {
                        nav.navigate(ChronolensNav.MediaGrid.name) {
                            popUpTo(nav.graph.startDestinationId) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    modifier = modifier
                ) {
                    Icon(
                        imageVector = Icons.Filled.Home,
                        contentDescription = null
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Albums Button
                Button(
                    onClick = {
                        nav.navigate(ChronolensNav.Albums.name) {
                            popUpTo(nav.graph.startDestinationId) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    modifier = modifier
                ) {
                    Icon(
                        imageVector = Icons.Filled.AccountBox,
                        contentDescription = null
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Search Button
                Button(
                    onClick = {
                        nav.navigate(ChronolensNav.Search.name) {
                            popUpTo(nav.graph.startDestinationId) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    modifier = modifier
                ) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Settings Button
                Button(
                    onClick = {
                        nav.navigate(ChronolensNav.Settings.name) {
                            popUpTo(nav.graph.startDestinationId) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    modifier = modifier
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = null
                    )
                }

                Spacer(modifier = Modifier.weight(0.5f))
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChronolensTopAppBar(
    canNavigateBack: Boolean,
    currentScreen: ChronolensNav,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    userLoginState: UserLoginState

) {

    if (userLoginState != UserLoginState.Loading && !noTopBar.contains(currentScreen)) {
        TopAppBar(
            title = { Text(text = currentScreen.name) },
            colors = TopAppBarDefaults.mediumTopAppBarColors(
                containerColor = Color.Transparent
            ),
            modifier = modifier,
            navigationIcon = {
                if (canNavigateBack) {
                    IconButton(onClick = navigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            }
        )
    }
}
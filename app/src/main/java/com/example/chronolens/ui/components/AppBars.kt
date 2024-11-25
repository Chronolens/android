package com.example.chronolens.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.navigation.NavHostController
import com.example.chronolens.R
import com.example.chronolens.utils.ChronolensNav
import com.example.chronolens.utils.noBottomBar
import com.example.chronolens.utils.noTopBar
import com.example.chronolens.viewModels.MediaGridState
import com.example.chronolens.viewModels.SelectingType
import com.example.chronolens.viewModels.SyncState
import com.example.chronolens.viewModels.UserLoginState

@Composable
fun ChronolensBottomBar(
    currentScreen: ChronolensNav,
    nav: NavHostController,
    navigationBarPadding: Dp
) {
    if (!noBottomBar.contains(currentScreen)) {
        BottomAppBar(
            modifier = Modifier
                .fillMaxWidth()
                .height(navigationBarPadding + 48.dp),
            containerColor = Color.Black
        ) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize()
            ) {
                val buttonWidth = maxWidth / 4
                val tertiaryColor = MaterialTheme.colorScheme.tertiary
                val defaultIconColor = MaterialTheme.colorScheme.onSecondary

                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.weight(0.5f))

                    IconButton(
                        onClick = {
                            nav.navigate(ChronolensNav.MediaGrid.name) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        modifier = Modifier.width(buttonWidth)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.housesimple),
                            contentDescription = "Home",
                            modifier = Modifier.size(32.dp),
                            tint = if (currentScreen == ChronolensNav.MediaGrid) tertiaryColor else defaultIconColor
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(
                        onClick = {
                            nav.navigate(ChronolensNav.Albums.name) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        modifier = Modifier.width(buttonWidth)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.foldersimple),
                            contentDescription = "Albums",
                            modifier = Modifier.size(32.dp),
                            tint = if (currentScreen == ChronolensNav.Albums) tertiaryColor else defaultIconColor
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(
                        onClick = {
                            nav.navigate(ChronolensNav.Search.name) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        modifier = Modifier.width(buttonWidth)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.magnifyingglass),
                            contentDescription = "Search",
                            modifier = Modifier.size(32.dp),
                            tint = if (currentScreen == ChronolensNav.Search) tertiaryColor else defaultIconColor
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(
                        onClick = {
                            nav.navigate(ChronolensNav.Settings.name) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        modifier = Modifier.width(buttonWidth)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.gear),
                            contentDescription = "Settings",
                            modifier = Modifier.size(32.dp),
                            tint = if (currentScreen == ChronolensNav.Settings) tertiaryColor else defaultIconColor
                        )
                    }

                    Spacer(modifier = Modifier.weight(0.5f))
                }
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
    userLoginState: UserLoginState,
    mediaGridState: State<MediaGridState>
) {

    var isPopupVisible by remember { mutableStateOf(false) }

    if (userLoginState != UserLoginState.Loading && !noTopBar.contains(currentScreen)) {
        TopAppBar(
            title = {
                Row(modifier = Modifier.fillMaxWidth()) {
                    if (mediaGridState.value.isSelecting) {
                        if (mediaGridState.value.selectingType == SelectingType.Remote) {
                            Text(stringResource(R.string.selecting_remotes))
                        } else {
                            Text(stringResource(R.string.selecting_locals))
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Text(text = mediaGridState.value.selected.size.toString())
                        Spacer(modifier = Modifier.padding(end = 16.dp))
                    } else {
                        Text(text = currentScreen.name)
                    }
                }
            },
            actions = {
                IconButton(onClick = { isPopupVisible = !isPopupVisible }) {
                    Icon(
                        painter = painterResource(id = R.drawable.gear),
                        contentDescription = null
                    )
                }

                DropdownMenu(
                    expanded = isPopupVisible,
                    onDismissRequest = { isPopupVisible = false },
                    properties = PopupProperties(
                        dismissOnClickOutside = true,
                        dismissOnBackPress = true,
                        focusable = true
                    ),
                    offset = DpOffset(x = 0.dp, y = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        // TODO: make these resource strings
                        Text(mediaGridState.value.syncState.name)
                        if (mediaGridState.value.syncState == SyncState.FetchingLocal) {
                            Text(mediaGridState.value.syncProgress.toString())
                        }
                    }
                }
            },
            colors = TopAppBarDefaults.mediumTopAppBarColors(
                containerColor = Color.Transparent
            ),
            modifier = modifier.border(2.dp, Color.White),
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
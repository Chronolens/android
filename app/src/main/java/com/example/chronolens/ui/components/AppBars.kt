package com.example.chronolens.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.navigation.NavHostController
import com.example.chronolens.R
import com.example.chronolens.ui.theme.defaultButtonColors
import com.example.chronolens.utils.ChronolensNav
import com.example.chronolens.utils.noBottomBar
import com.example.chronolens.utils.noTopBar
import com.example.chronolens.viewModels.MediaGridViewModel
import com.example.chronolens.viewModels.MediaGridState
import com.example.chronolens.viewModels.SelectingType
import com.example.chronolens.viewModels.SyncState
import com.example.chronolens.viewModels.UserLoginState
import kotlinx.coroutines.launch

@Composable
fun ChronolensBottomBar(
    currentScreen: ChronolensNav,
    nav: NavHostController,
    navigationBarPadding: Dp,
    mediaGridState: State<MediaGridState>,
    mediaGridViewModel: MediaGridViewModel
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
                when {
                    mediaGridState.value.isSelectingPerson -> {
                        SelectingPersonsBottomBar(
                            state = mediaGridState,
                            viewModel = mediaGridViewModel
                        )
                    }

                    mediaGridState.value.isSelecting -> {
                        SelectingBottomBar(
                            state = mediaGridState,
                            viewModel = mediaGridViewModel,
                            buttonWidth = buttonWidth
                        )
                    }

                    else -> {
                        NavigationBottomBar(
                            currentScreen = currentScreen,
                            nav = nav,
                            buttonWidth = buttonWidth,
                            lazyGridState = mediaGridViewModel.lazyGridState
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectingPersonsBottomBar(
    state: State<MediaGridState>,
    viewModel: MediaGridViewModel
) {
    val buttonText = if (state.value.selectedPeople.size == 1) {
        stringResource(id = R.string.rename)
    } else {
        stringResource(id = R.string.group_people)
    }

    val onClickAction = {
        viewModel.confirmPersonClustering()
    }

    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = onClickAction,
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp),
            colors = defaultButtonColors()
        ) {
            Text(
                text = buttonText,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}


@Composable
private fun SelectingBottomBar(
    state: State<MediaGridState>,
    viewModel: MediaGridViewModel,
    buttonWidth: Dp
) {

    val context = LocalContext.current

    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        if (state.value.selectingType == SelectingType.Local) {
            IconButton(
                onClick = {
                    viewModel.shareLocalImages(context, state.value.selected.values.toList())
                },
                modifier = Modifier.width(buttonWidth)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.share),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                )
            }

//          Spacer(modifier = Modifier.weight(1f))

            IconButton(
                onClick = {
                    viewModel.uploadMultipleMedia(state.value.selected.values.toList())
                },
                modifier = Modifier.width(buttonWidth)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.uploadsimple),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                )
            }
        } else if (state.value.selectingType == SelectingType.Remote) {
            IconButton(
                onClick = {
                    viewModel.downloadMultipleMedia(state.value.selected.values.toList(), context)
                },
                modifier = Modifier.width(buttonWidth)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.downloadsimple),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                )
            }
        }
    }

}

@Composable
private fun NavigationBottomBar(
    currentScreen: ChronolensNav,
    nav: NavHostController,
    buttonWidth: Dp,
    lazyGridState: LazyGridState
) {

    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val defaultIconColor = MaterialTheme.colorScheme.onSecondary
    val coroutineScope = rememberCoroutineScope()

    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.weight(0.5f))

        IconButton(
            onClick = {
                if (currentScreen == ChronolensNav.MediaGrid) {
                    coroutineScope.launch {
                        // TODO: with or without animation?
//                        lazyGridState.scrollToItem(0)
                        lazyGridState.animateScrollToItem(0)
                    }
                } else {
                    nav.navigate(ChronolensNav.MediaGrid.name) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChronolensTopAppBar(
    canNavigateBack: Boolean,
    currentScreen: ChronolensNav,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    userLoginState: UserLoginState,
    mediaGridState: State<MediaGridState>,
    mediaGridViewModel: MediaGridViewModel
) {

    var isPopupVisible by remember { mutableStateOf(false) }

    if (userLoginState != UserLoginState.Loading && !noTopBar.contains(currentScreen)) {
        TopAppBar(
            title = {
                when {
                    mediaGridState.value.isSelectingPerson -> {
                        SelectingPersonTopAppBarTitle(
                            state = mediaGridState,
                            mediaGridViewModel = mediaGridViewModel
                        )
                    }
                    mediaGridState.value.isSelecting -> {
                        SelectingMediaTopAppBarTitle(
                            state = mediaGridState,
                            mediaGridViewModel = mediaGridViewModel
                        )
                    }
                    else -> {
                        Text(text = getScreenTitle(currentScreen))
                    }
                }
            },
            actions = {
                IconButton(onClick = { isPopupVisible = !isPopupVisible }) {
                    Icon(
                        painter = painterResource(id = R.drawable.aperture),
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
                        // TODO: pretty print here
                        when (mediaGridState.value.syncState) {
                            SyncState.Synced -> Text(stringResource(R.string.sync_state_synced))
                            SyncState.FetchingRemote -> Text(stringResource(R.string.sync_state_fetching_remote))
                            SyncState.FetchingLocal -> {
                                val prog = mediaGridState.value.syncProgress
                                Text(stringResource(R.string.sync_state_fetching_local))
                                Text(
                                    stringResource(
                                        R.string.sync_state_fetching_local_progress,
                                        prog.first,
                                        prog.second
                                    )
                                )
                            }

                            SyncState.Merging -> Text(stringResource(R.string.sync_state_merging))
                        }
                        if (mediaGridState.value.isUploading) {
                            val (progress, max) = mediaGridState.value.uploadProgress
                            Text(stringResource(R.string.select_upload, progress, max))
                        }
                        if (mediaGridState.value.isDownloading) {
                            val (progress, max) = mediaGridState.value.downloadProgress
                            Text(stringResource(R.string.select_download, progress, max))
                        }
                    }
                }
            },
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

@Composable
private fun SelectingMediaTopAppBarTitle(
    state: State<MediaGridState>,
    mediaGridViewModel: MediaGridViewModel
) {
    val selectingType = state.value.selectingType
    Row(modifier = Modifier.fillMaxWidth()) {
        Icon(
            imageVector = Icons.Default.Clear,
            contentDescription = null,
            modifier = Modifier.clickable {
                mediaGridViewModel.deselectAll()
            }
        )
        Spacer(modifier = Modifier.width(8.dp))

        when (selectingType) {
            SelectingType.Remote -> {
                Text(stringResource(R.string.selecting_remotes))
            }
            else -> {
                Text(stringResource(R.string.selecting_locals))
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Text(text = state.value.selected.size.toString())
        Spacer(modifier = Modifier.padding(end = 16.dp))
    }
}

@Composable
private fun SelectingPersonTopAppBarTitle(
    state: State<MediaGridState>,
    mediaGridViewModel: MediaGridViewModel
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Icon(
            imageVector = Icons.Default.Clear,
            contentDescription = null,
            modifier = Modifier.clickable {
                mediaGridViewModel.deselectPeople()
            }
        )
        Spacer(modifier = Modifier.width(8.dp))

        Text(stringResource(R.string.selecting_persons))

        Spacer(modifier = Modifier.weight(1f))
        Text(text = state.value.selectedPeople.size.toString())
        Spacer(modifier = Modifier.padding(end = 16.dp))
    }
}


@Composable
fun getScreenTitle(nav: ChronolensNav): String {
    return when (nav) {
        ChronolensNav.Login -> stringResource(R.string.login_screen)
        ChronolensNav.MediaGrid -> stringResource(R.string.media_gallery)
        ChronolensNav.FullScreenMedia -> stringResource(R.string.media_viewer)
        ChronolensNav.Albums -> stringResource(R.string.albums)
        ChronolensNav.PersonPhotoGrid -> stringResource(R.string.person_photos)
        ChronolensNav.Search -> stringResource(R.string.search)
        ChronolensNav.Settings -> stringResource(R.string.settings)
        ChronolensNav.BackgroundUpload -> stringResource(R.string.background_uploads)
        ChronolensNav.ActivityHistory -> stringResource(R.string.activity_history)
        ChronolensNav.MachineLearning -> stringResource(R.string.machine_learning)
        ChronolensNav.AlbumsPicker -> "Album Selection"
    }
}


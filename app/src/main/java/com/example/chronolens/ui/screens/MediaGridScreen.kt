package com.example.chronolens.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.size.Scale
import com.example.chronolens.R
import com.example.chronolens.models.LocalMedia
import com.example.chronolens.models.MediaAsset
import com.example.chronolens.models.RemoteMedia
import com.example.chronolens.utils.ChronolensNav
import com.example.chronolens.viewModels.MediaGridScreenViewModel
import com.example.chronolens.viewModels.MediaGridState
import com.example.chronolens.viewModels.WorkManagerViewModel

// TODO: maybe make every single sync a background job in case it is too long
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MediaGridScreen(
    viewModel: MediaGridScreenViewModel,
    state: State<MediaGridState>,
    navController: NavController,
    work: WorkManagerViewModel,
    modifier: Modifier,
    refreshPaddingValues: Dp
) {
    val pullRefreshState = rememberPullRefreshState(
        refreshing = state.value.isLoading,
        onRefresh = viewModel::refreshMediaGrid,
        refreshingOffset = refreshPaddingValues,
        refreshThreshold = refreshPaddingValues
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pullRefresh(pullRefreshState)
            .then(modifier)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.fillMaxSize()
        ) {
            items(state.value.media) { asset ->
                ImageItem(viewModel, asset) {
                    viewModel.updateCurrentAsset(asset)
                    navController.navigate(ChronolensNav.FullScreenMedia.name) {
                        popUpTo(ChronolensNav.FullScreenMedia.name) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
        }

        PullRefreshIndicator(
            refreshing = state.value.isLoading,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}


// TODO : Investigate the possibility of having a composable builder in the class itself to reduce conditional logic
@Composable
fun ImageItem(
    viewModel: MediaGridScreenViewModel,
    mediaAsset: MediaAsset,
    onClick: (MediaAsset) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .padding(1.dp)
            .fillMaxWidth()
            .aspectRatio(1f),
        contentAlignment = Alignment.Center,
    ) {

        if (mediaAsset is LocalMedia) {
            Box {

                if (mediaAsset.thumbnail != null) {
                    Image(
                        bitmap = mediaAsset.thumbnail!!.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                onClick(mediaAsset)
                            }
                    )
                } else {
                    val context = LocalContext.current
                    val model = ImageRequest.Builder(context)
                        .data(mediaAsset.path)
                        .size(120)
                        .scale(Scale.FILL)
                        .build()
                    AsyncImage(
                        model = model,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                onClick(mediaAsset)
                            }
                    )
                }

                if (mediaAsset.remoteId != null) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.cloudcheck),
                        contentDescription = null,
                        tint = colorScheme.tertiary,
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .align(Alignment.TopEnd)
                            .padding(horizontal = 4.dp, vertical = 4.dp)
                    )
                }
            }
        } else if (mediaAsset is RemoteMedia) {
            val remoteAsset: RemoteMedia = mediaAsset
            var imageUrl by remember { mutableStateOf<String?>(null) }

            LaunchedEffect(mediaAsset) {
                val url = viewModel.getRemoteAssetPreviewUrl(remoteAsset.id)
                imageUrl = url
            }
            Box {
                if (imageUrl != null) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                onClick(mediaAsset)
                            },
                    )
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.cloud),
                        contentDescription = null,
                        tint = colorScheme.tertiary,
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .align(Alignment.TopEnd)
                            .padding(horizontal = 4.dp, vertical = 4.dp)
                    )
                } else {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}

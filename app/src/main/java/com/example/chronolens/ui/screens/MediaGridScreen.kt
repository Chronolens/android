package com.example.chronolens.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.CancellationSignal
import android.provider.MediaStore
import android.util.Size
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.chronolens.R
import com.example.chronolens.models.LocalMedia
import com.example.chronolens.models.MediaAsset
import com.example.chronolens.models.RemoteMedia
import com.example.chronolens.utils.ChronolensNav
import com.example.chronolens.viewModels.MediaGridScreenViewModel
import com.example.chronolens.viewModels.MediaGridState
import com.example.chronolens.viewModels.SelectingType
import com.example.chronolens.viewModels.WorkManagerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
            items(items = state.value.media) { asset ->
                ImageItem(
                    viewModel = viewModel,
                    mediaAsset = asset,
                    goToImage = {
                        viewModel.updateCurrentAsset(asset)
                        navController.navigate(ChronolensNav.FullScreenMedia.name) {
                            popUpTo(ChronolensNav.FullScreenMedia.name) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    selectOrDeselect = {
                        viewModel.selectOrDeselect(asset.checksum!!, asset)
                    },
                    state = state
                )
            }
        }

        PullRefreshIndicator(
            refreshing = state.value.isLoading,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}


fun loadThumbnail(context: Context, uri: Uri, width: Int, height: Int): Bitmap? {
    val size = Size(width, height)
    val cancellationSignal = CancellationSignal()

    return try {
        context.contentResolver.loadThumbnail(uri, size, cancellationSignal)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// TODO : Investigate the possibility of having a composable builder in the class itself to reduce conditional logic
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImageItem(
    viewModel: MediaGridScreenViewModel,
    mediaAsset: MediaAsset,
    goToImage: () -> Unit,
    selectOrDeselect: () -> Unit,
    state: State<MediaGridState>
) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .padding(1.dp)
            .fillMaxWidth()
            .aspectRatio(1f),
        contentAlignment = Alignment.Center,
    ) {
        if (mediaAsset is LocalMedia) {
            var thumbnail by remember { mutableStateOf(mediaAsset.thumbnail?.asImageBitmap()) }

            LaunchedEffect(mediaAsset) {
                if (mediaAsset.thumbnail == null) {
                    withContext(Dispatchers.IO) {
                        val uri = Uri.withAppendedPath(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            mediaAsset.id.toString()
                        )
                        val bitmap = loadThumbnail(context, uri, 120, 120)
                        mediaAsset.thumbnail = bitmap
                        thumbnail = bitmap?.asImageBitmap()
                    }
                }
            }

            if (thumbnail != null) {
                Image(
                    bitmap = thumbnail!!,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .combinedClickable(
                            enabled = state.value.selectingType != SelectingType.Remote,
                            onClick = {
                                if (state.value.isSelecting) {
                                    selectOrDeselect()
                                } else {
                                    goToImage()
                                }
                            },
                            onLongClick = selectOrDeselect
                        ),
                    colorFilter = if (state.value.selectingType == SelectingType.Remote) ColorFilter.colorMatrix(
                        ColorMatrix().apply { setToSaturation(0f) }) else null
                )
            } else {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
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

            if (state.value.selected.containsKey(mediaAsset.checksum!!)) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.checkcircle),
                    contentDescription = null,
                    tint = colorScheme.tertiary,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .align(Alignment.TopStart)
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                )
            }
        } else if (mediaAsset is RemoteMedia) {
            var imageUrl by remember { mutableStateOf<String?>(null) }

            LaunchedEffect(mediaAsset) {
                val url = viewModel.getRemoteAssetPreviewUrl(mediaAsset.id)
                imageUrl = url
            }
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .combinedClickable(
                            enabled = state.value.selectingType != SelectingType.Local,
                            onClick = {
                                if (state.value.isSelecting) {
                                    selectOrDeselect()
                                } else {
                                    goToImage()
                                }
                            },
                            onLongClick = selectOrDeselect
                        ),
                    colorFilter = if (state.value.selectingType == SelectingType.Local) ColorFilter.colorMatrix(
                        ColorMatrix().apply { setToSaturation(0f) }) else null
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

            if (state.value.selected.containsKey(mediaAsset.checksum!!)) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.checkcircle),
                    contentDescription = null,
                    tint = colorScheme.tertiary,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .align(Alignment.TopStart)
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                )
            }

        }
    }
}

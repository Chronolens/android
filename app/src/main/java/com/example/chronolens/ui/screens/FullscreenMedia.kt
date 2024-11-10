package com.example.chronolens.ui.screens

import android.util.Log
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil3.compose.rememberAsyncImagePainter
import com.example.chronolens.R
import com.example.chronolens.models.LocalMedia
import com.example.chronolens.models.MediaAsset
import com.example.chronolens.models.RemoteMedia
import com.example.chronolens.ui.components.MetadataDisplay
import com.example.chronolens.utils.loadExifData
import com.example.chronolens.viewModels.FullscreenImageState
import com.example.chronolens.viewModels.MediaGridScreenViewModel
import com.example.chronolens.viewModels.MediaGridState


// TODO: Restrict photo vertical position while zooming in with double tap

// TODO: METADATA SLIDING BOX SWIPE IS TAKING OVER ZOOMING GESTURES

// TODO: Move the photo slightly up when the metadata box is visible



@Composable
fun FullscreenMediaView(
    viewModel: MediaGridScreenViewModel,
    mediaGridState: State<MediaGridState>,
    fullscreenMediaState: State<FullscreenImageState>,
    navController: NavHostController,
    modifier: Modifier
) {
    val boxHeight = 300.dp

    val mediaAsset = fullscreenMediaState.value.currentMedia
    var isBoxVisible by remember { mutableStateOf(false) }
    var metadata by remember { mutableStateOf<Map<String, String?>>(emptyMap()) }

    val systemNavBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val boxOffsetY by animateDpAsState(targetValue = if (isBoxVisible) 0.dp else boxHeight + systemNavBarHeight)

    if (mediaAsset is LocalMedia) {
        metadata = loadExifData(mediaAsset.path)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        LoadFullImage(
            mediaAsset!!,
            viewModel,
            { isBoxVisible = false },
            { isBoxVisible = true },
            isBoxVisible
        )

        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BackButton(navController)
            BookmarkButton()
        }

        // Bottom Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 4.dp)
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DeleteOrTransferButton(mediaAsset)
            Spacer(modifier = Modifier.width(16.dp))

            ShareButton()
            Spacer(modifier = Modifier.width(16.dp))

            UploadOrRemoveButton(mediaAsset, viewModel)
            Spacer(modifier = Modifier.width(16.dp))

            MenuButton({ isBoxVisible = true })
        }

        // Metadata Box
        val colorScheme = MaterialTheme.colorScheme
        val brush = Brush.horizontalGradient(
            colors = listOf(
                colorScheme.primary,
                colorScheme.secondary
            )
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(boxHeight)
                .align(Alignment.BottomCenter)
                .offset(y = boxOffsetY)
                .background(brush)
        ) {
            MetadataDisplay(metadata, mediaAsset.timestamp)
        }
    }
}


@Composable
fun BackButton(navController: NavHostController) {
    IconButton(onClick = { navController.navigateUp() }) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = Color.White
        )
    }
}

@Composable
fun BookmarkButton() {
    IconButton(onClick = { Log.i("FullscreenMediaView", "Bookmark button pressed") }) {
        Icon(
            imageVector = Icons.Default.FavoriteBorder,
            contentDescription = "Bookmark",
            tint = Color.White
        )
    }
}


@Composable
fun ShareButton() {
    IconButton(onClick = { Log.i("FullscreenMediaView", "Share button pressed") }) {
        Icon(
            imageVector = Icons.Default.Share,
            contentDescription = "Share",
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}


@Composable
fun MenuButton(showBox: () -> Unit) {
    IconButton(
        onClick = {
            Log.i("FullscreenMediaView", "Menu button pressed")
            showBox()
        }
    ) {
        Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = "Menu",
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}




@Composable
fun LoadFullImage(
    mediaAsset: MediaAsset,
    viewModel: MediaGridScreenViewModel,
    hideBox: () -> Unit,
    showBox: () -> Unit,
    isBoxVisible: Boolean
) {

    if (mediaAsset is RemoteMedia) {
        var imageUrl by remember { mutableStateOf<String?>(null) }
        LaunchedEffect(mediaAsset) {
            val url = viewModel.getRemoteAssetFullImageUrl(mediaAsset.id)
            imageUrl = url
        }
        if (imageUrl != null) {
            ImageDisplay(
                photoURI = imageUrl!!,
                hideBox = hideBox,
                showBox = showBox,
                isBoxVisible = isBoxVisible
            )
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { // TODO DOES NOTHING?
                CircularProgressIndicator()
            }
        }
    } else if (mediaAsset is LocalMedia) {
        ImageDisplay(
            photoURI = mediaAsset.path,
            hideBox = hideBox,
            showBox = showBox,
            isBoxVisible = isBoxVisible
        )
    }
}


// TODO: detectTransformGestures is not fine grain enough, we will need to listen to raw events and apply the calculations manually

@Composable
private fun ImageDisplay(
    photoURI: String,
    modifier: Modifier = Modifier,
    hideBox: () -> Unit,
    showBox: () -> Unit,
    isBoxVisible: Boolean
) {
    var offset by remember { mutableStateOf(Offset.Zero) }
    var zoom by remember { mutableStateOf(1f) }

    if (isBoxVisible) {
        zoom = 1f
        offset = Offset.Zero
    }

    val verticalDragModifier = Modifier.pointerInput(Unit) {
        detectVerticalDragGestures(
            onVerticalDrag = { _, dragAmount ->
                if (zoom == 1f) {
                    if (dragAmount < -10) {
                        showBox()
                    } else if (dragAmount > 10) {
                        hideBox()
                    }
                }
            }
        )
    }

    val transformGestureModifier = if (!isBoxVisible) Modifier.pointerInput(Unit) {
        detectTransformGestures { centroid, pan, gestureZoom, _ ->
            offset = offset.calculateNewOffset(
                centroid, pan, zoom, gestureZoom, size
            )
            zoom = maxOf(1f, zoom * gestureZoom)
        }
    } else Modifier

    val tapGestureModifier = if (!isBoxVisible) Modifier.pointerInput(Unit) {
        detectTapGestures(
            onDoubleTap = { tapOffset ->
                Log.d("ImageDisplay", "Double-tap coordinates: x=${tapOffset.x}, y=${tapOffset.y}")

                zoom = if (zoom > 1f) 1f else 2f
                offset = calculateDoubleTapOffset(zoom, size, tapOffset)
            }
        )
    } else Modifier

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(tapGestureModifier)
            .then(transformGestureModifier)
            .graphicsLayer {
                translationX = -offset.x * zoom
                translationY = -offset.y * zoom
                scaleX = zoom; scaleY = zoom
                transformOrigin = TransformOrigin(0f, 0f)
            }
            .clipToBounds()
            .then(verticalDragModifier)
    ) {
        Image(
            painter = rememberAsyncImagePainter(photoURI),
            contentDescription = "",
            modifier = modifier.align(Alignment.Center)
        )
    }
}


fun Offset.calculateNewOffset(
    centroid: Offset,
    pan: Offset,
    zoom: Float,
    gestureZoom: Float,
    size: IntSize
): Offset {
    val newScale = maxOf(1f, zoom * gestureZoom)
    val newOffset = (this + centroid / zoom) -
            (centroid / newScale + pan / zoom)
    return Offset(
        newOffset.x.coerceIn(0f, (size.width / zoom) * (zoom - 1f)),
        newOffset.y.coerceIn(0f, (size.height / zoom) * (zoom - 1f))
    )
}

fun calculateDoubleTapOffset(
    zoom: Float,
    size: IntSize,
    tapOffset: Offset
): Offset {
    val targetOffsetX = tapOffset.x - (size.width / (2 * zoom))
    val targetOffsetY = tapOffset.y - (size.height / (2 * zoom))

    val constrainedOffsetX = targetOffsetX.coerceIn(0f, size.width * (zoom - 1) / zoom)
    val constrainedOffsetY = targetOffsetY.coerceIn(0f, size.height * (zoom - 1) / zoom)

    Log.d("ImageDisplay", "Calculated Offset: x=$constrainedOffsetX, y=$constrainedOffsetY")

    return Offset(constrainedOffsetX, constrainedOffsetY)
}

@Composable
fun UploadOrRemoveButton(asset: MediaAsset, viewModel: MediaGridScreenViewModel) {
    if (asset is LocalMedia) {
        if (asset.remoteId != null) {
            IconButton(
                onClick = {
                    Log.i("UploadOrRemove", "Remove from cloud not implemented yet")
                },
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.cloudcheck),
                    contentDescription = "Uploaded",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(24.dp)
                )
            }
        } else {
            IconButton(
                onClick = {
                    Log.i("UploadOrRemove", "Uploading local asset: ${asset.path}")
                    viewModel.uploadMedia(asset)
                }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.uploadsimple),
                    contentDescription = "Upload",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    } else if (asset is RemoteMedia) {
        IconButton(
            onClick = {
                Log.i("UploadOrRemove", "Remove from cloud not implemented yet")
            }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.cloud),
                contentDescription = "Cloud",
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun DeleteOrTransferButton(asset: MediaAsset) {
    if (asset is RemoteMedia) {
        IconButton(onClick = {
            Log.i("DeleteOrTransferIcon", "Downloading not implemented yet")
        }) {
            Icon(
                painter = painterResource(id = R.drawable.downloadsimple),
                contentDescription = "Download",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    } else if (asset is LocalMedia) {
        IconButton(onClick = {
            Log.i("DeleteOrTransferIcon", "Deleting not implemented yet")
        }) {
            Icon(
                painter = painterResource(id = R.drawable.trashsimple),
                contentDescription = "Delete",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}


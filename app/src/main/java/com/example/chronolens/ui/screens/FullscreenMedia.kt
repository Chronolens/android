package com.example.chronolens.ui.screens

import android.util.Log
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil3.compose.rememberAsyncImagePainter
import com.example.chronolens.models.FullMedia
import com.example.chronolens.models.LocalMedia
import com.example.chronolens.models.MediaAsset
import com.example.chronolens.models.RemoteMedia
import com.example.chronolens.ui.components.BackButton
import com.example.chronolens.ui.components.BookmarkButton
import com.example.chronolens.ui.components.DeleteOrDownloadButton
import com.example.chronolens.ui.components.MenuButton
import com.example.chronolens.ui.components.MetadataDisplay
import com.example.chronolens.ui.components.ShareButton
import com.example.chronolens.ui.components.UploadOrRemoveButton
import com.example.chronolens.viewModels.FullscreenImageState
import com.example.chronolens.viewModels.MediaGridViewModel
import com.example.chronolens.viewModels.MediaGridState

// TODO: Restrict photo vertical position while zooming in with double tap
// TODO: Move the photo slightly up when the metadata box is visible

@Composable
fun FullscreenMediaView(
    viewModel: MediaGridViewModel,
    mediaGridState: State<MediaGridState>,
    fullscreenMediaState: State<FullscreenImageState>,
    navController: NavHostController,
    modifier: Modifier
) {

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    val boxHeight = 300.dp

    val mediaAsset = fullscreenMediaState.value.currentMediaAsset
    val fullMedia = fullscreenMediaState.value.currentFullMedia

    var isBoxVisible by remember { mutableStateOf(false) }

    val systemNavBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val boxOffsetY by animateDpAsState(targetValue = if (isBoxVisible) 0.dp else boxHeight + systemNavBarHeight)


    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (mediaAsset == null || fullMedia == null) {
            CircularProgressIndicator()
            return@Box
        }
        LoadFullImage(
            mediaAsset = mediaAsset,
            hideBox = { isBoxVisible = false },
            showBox = { isBoxVisible = true },
            isBoxVisible = isBoxVisible,
            fullMedia = fullMedia
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
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DeleteOrDownloadButton(
                asset = mediaAsset,
                viewModel = viewModel,
                state = fullscreenMediaState
            )
//          Spacer(modifier = Modifier.width(16.dp))

            if (mediaAsset is LocalMedia) {
                ShareButton(mediaAsset)
            }

            UploadOrRemoveButton(
                asset = mediaAsset,
                viewModel = viewModel,
                state = fullscreenMediaState
            )
//          Spacer(modifier = Modifier.width(16.dp))

            MenuButton({ isBoxVisible = true })
        }


        // Metadata Box
        val colorScheme = MaterialTheme.colorScheme
        val brush = Brush.horizontalGradient(
            colors = listOf(
                colorScheme.secondary,
                colorScheme.primary
            ),
            startX = 0f,
            endX = with(LocalDensity.current) { screenWidth.toPx() } // This is odd, perhaps theres a better way
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(boxHeight)
                .align(Alignment.BottomCenter)
                .offset(y = boxOffsetY)
                .background(brush)
        ) {
            MetadataDisplay(fullMedia)
        }
    }
}


@Composable
fun LoadFullImage(
    mediaAsset: MediaAsset,
    hideBox: () -> Unit,
    showBox: () -> Unit,
    isBoxVisible: Boolean,
    fullMedia: FullMedia
) {

    if (mediaAsset is RemoteMedia) {
        var imageUrl by remember { mutableStateOf<String?>(null) }
        LaunchedEffect(mediaAsset) {
            imageUrl = fullMedia.mediaUrl
        }
        if (imageUrl != null) {
            ImageDisplay(
                photoURI = imageUrl!!,
                hideBox = hideBox,
                showBox = showBox,
                isBoxVisible = isBoxVisible
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
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

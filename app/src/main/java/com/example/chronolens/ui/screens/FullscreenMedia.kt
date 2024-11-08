package com.example.chronolens.ui.screens

import android.util.Log
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil3.compose.rememberAsyncImagePainter
import com.example.chronolens.models.LocalMedia
import com.example.chronolens.models.MediaAsset
import com.example.chronolens.models.RemoteMedia
import com.example.chronolens.viewModels.FullscreenImageState
import com.example.chronolens.viewModels.MediaGridScreenViewModel
import com.example.chronolens.viewModels.MediaGridState


// TODO: This is not using full quality image as we can verify zooming in, perhaps it creates
// a image with the initial width we give it and then it loses detail since it resizes the image?


// TODO: Restrict photo vertical position while zooming in with double tap


val boxHeight = 300.dp

@Composable
fun FullscreenMediaView(
    viewModel: MediaGridScreenViewModel,
    mediaGridState: State<MediaGridState>,
    fullscreenMediaState: State<FullscreenImageState>,
    navController: NavHostController
) {
    val mediaAsset = fullscreenMediaState.value.currentMedia
    var isBoxVisible by remember { mutableStateOf(false) }


    val boxOffsetY by animateDpAsState(targetValue = if (isBoxVisible) 0.dp else boxHeight)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {

        LoadFullImage(mediaAsset!!, viewModel, { isBoxVisible = false }, { isBoxVisible = true })

        // Top "Bar" - hovering return and favorite buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp, horizontal = 16.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            IconButton(onClick = { println("Bookmark button pressed") }) {
                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    contentDescription = "Bookmark",
                    tint = Color.White
                )
            }
        }

        // Bottom "Bar" - metadata - share - upload - delete // TODO: Change metadata and deletes positions maybe
         Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 48.dp, horizontal = 16.dp)
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { println("Menu button pressed") }) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = Color.White
                )
            }
            IconButton(onClick = { println("Share button pressed") }) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = Color.White
                )
            }
            CloudIcon(mediaAsset, viewModel)
            DeleteOrTransferIcon(mediaAsset)
        }

        // Metadata sliding box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(boxHeight)
                .align(Alignment.BottomCenter)
                .offset(y = boxOffsetY)
                .background(Color.Gray)
        )
    }
}


@Composable
fun LoadFullImage(
    mediaAsset: MediaAsset,
    viewModel: MediaGridScreenViewModel,
    hideBox: () -> Unit,
    showBox: () -> Unit
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
                showBox = showBox
            )
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    } else if (mediaAsset is LocalMedia) {
        ImageDisplay(
            photoURI = mediaAsset.path,
            hideBox = hideBox,
            showBox = showBox
        )
    }
}



// TODO: detectTransformGestures is not fine grain enough, we will need to listen to raw events and apply the calculations manually

@Composable
private fun ImageDisplay (
    photoURI : String,
    modifier: Modifier = Modifier,
    hideBox: () -> Unit,
    showBox: () -> Unit){

    var offset by remember { mutableStateOf(Offset.Zero) }
    var zoom by remember { mutableStateOf(1f) }

    val verticalDragModifier = Modifier.pointerInput(Unit) {
        detectVerticalDragGestures(
            onVerticalDrag = { _, dragAmount ->
                if (zoom == 1f) {  // Only allow swipe gestures when fully zoomed out
                    if (dragAmount < -10) {
                        showBox()
                    } else if (dragAmount > 10) {
                        hideBox()
                    }
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit){
                detectTapGestures(
                    onDoubleTap = { tapOffset ->
                        Log.d("ImageDisplay", "Double-tap coordinates: x=${tapOffset.x}, y=${tapOffset.y}")

                        zoom = if (zoom > 1f) 1f else 2f
                        offset = calculateDoubleTapOffset(zoom, size, tapOffset)
                    }
                )
            }
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, gestureZoom, _ ->
                    offset = offset.calculateNewOffset(
                        centroid, pan, zoom, gestureZoom, size
                    )
                    zoom = maxOf(1f, zoom * gestureZoom)
                }
            }
            .graphicsLayer {
                translationX = -offset.x * zoom
                translationY = -offset.y * zoom
                scaleX = zoom; scaleY = zoom
                transformOrigin = TransformOrigin(0f, 0f)
            }

            .clipToBounds()
            .then(verticalDragModifier)
    ){
        Image(
            painter = rememberAsyncImagePainter(photoURI),
            contentDescription = "",
            modifier = modifier
                .align(Alignment.Center)
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
fun CloudIcon(asset: MediaAsset, viewModel: MediaGridScreenViewModel) {
    if (asset is LocalMedia) {
        if (asset.remoteId != null) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Uploaded",
                tint = Color.White
            )
        } else {
            IconButton(onClick = {
                println("Uploading local asset: ${asset.path}")
                viewModel.uploadMedia(asset)
            }) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Upload",
                    tint = Color.White
                )
            }
        }
    } else if (asset is RemoteMedia) {
        IconButton(onClick = {
            println("Remove from cloud not implemented yet")
        }) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Cloud",
                tint = Color.White
            )
        }
    }
}

@Composable
fun DeleteOrTransferIcon(asset: MediaAsset) {
    if (asset is RemoteMedia) {
        IconButton(onClick = {
            println("Downloading not implemented yet")
        }) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Download",
                tint = Color.White
            )
        }
    } else if (asset is LocalMedia) {
        IconButton(onClick = {
            println("Deleting not implemented yet")
        }) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                tint = Color.White
            )
        }
    }
}

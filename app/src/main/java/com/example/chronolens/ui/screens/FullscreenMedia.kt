package com.example.chronolens.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.TransformableState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.example.chronolens.models.LocalMedia
import com.example.chronolens.models.MediaAsset
import com.example.chronolens.models.RemoteMedia
import com.example.chronolens.viewModels.FullscreenImageState
import com.example.chronolens.viewModels.MediaGridScreenViewModel
import com.example.chronolens.viewModels.MediaGridState

@Composable
fun FullscreenMediaView(
    viewModel: MediaGridScreenViewModel,
    mediaGridState: State<MediaGridState>,
    fullscreenMediaState: State<FullscreenImageState>,
    navController: NavHostController
) {
    val mediaAsset = fullscreenMediaState.value.currentMedia

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LoadFullImage(mediaAsset!!, viewModel)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp, horizontal = 16.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { navController.navigateUp() }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            IconButton(
                onClick = { println("Bookmark button pressed") }
            ) {
                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    contentDescription = "Bookmark",
                    tint = Color.White
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 48.dp, horizontal = 16.dp)
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceBetween

        ) {
            IconButton(
                onClick = { println("Menu button pressed") }
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = Color.White
                )
            }
            IconButton(
                onClick = { println("Share button pressed") }
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = Color.White
                )
            }
            CloudIcon(mediaAsset, viewModel)
            DeleteOrTransferIcon(mediaAsset)
        }
    }
}

// TODO: temporary only
@Composable
fun metadataDisplay(){

}

@Composable
fun LoadFullImage(mediaAsset: MediaAsset, viewModel: MediaGridScreenViewModel) {
    val scale = remember { mutableStateOf(1f) }
    val offset = remember { mutableStateOf(Offset(0f, 0f)) }

    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val density = LocalDensity.current

    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        scale.value = (scale.value * zoomChange).coerceIn(1f, 4f)

        val maxXOffset = with(density) { (screenWidth * (scale.value - 1)).toPx() / 2 }
        val maxYOffset = with(density) { (screenHeight * (scale.value - 1)).toPx() / 2 }

        offset.value = Offset(
            x = (offset.value.x + panChange.x * scale.value).coerceIn(-maxXOffset, maxXOffset),
            y = (offset.value.y + panChange.y * scale.value).coerceIn(-maxYOffset, maxYOffset)
        )
    }

    val maxZoom = 2f
    val defaultZoom = 1f

    val doubleTapModifier = Modifier.pointerInput(Unit) {
        detectTapGestures(
            onDoubleTap = { tapOffset ->
                val imageWidth = screenWidth.toPx() * scale.value
                val imageHeight = screenHeight.toPx() * scale.value

                if (scale.value > defaultZoom) {
                    scale.value = defaultZoom
                    offset.value = Offset.Zero
                } else {
                    scale.value = maxZoom

                    val maxXOffset = with(density) { (screenWidth * (scale.value - 1)).toPx() / 2 }
                    val maxYOffset = with(density) { (screenHeight * (scale.value - 1)).toPx() / 2 }

                    offset.value = Offset(
                        x = (screenWidth.toPx() / 2 - (tapOffset.x * scale.value - imageWidth / 2)).coerceIn(-maxXOffset, maxXOffset),
                        y = (screenHeight.toPx() / 2 - (tapOffset.y * scale.value - imageHeight / 2)).coerceIn(-maxYOffset, maxYOffset)
                    )
                }
            }
        )
    }

    if (mediaAsset is RemoteMedia) {
        var imageUrl by remember { mutableStateOf<String?>(null) }
        LaunchedEffect(mediaAsset) {
            val url = viewModel.getRemoteAssetFullImageUrl(mediaAsset.id)
            imageUrl = url
        }
        if (imageUrl != null) {
            ZoomableImage(
                url = imageUrl!!,
                scale = scale.value,
                offset = offset.value,
                transformableState = transformableState,
                doubleTapModifier = doubleTapModifier
            )
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    } else if (mediaAsset is LocalMedia) {
        ZoomableImage(
            url = mediaAsset.path,
            scale = scale.value,
            offset = offset.value,
            transformableState = transformableState,
            doubleTapModifier = doubleTapModifier
        )
    }
}

@Composable
fun ZoomableImage(
    url: String,
    scale: Float,
    offset: Offset,
    transformableState: TransformableState,
    doubleTapModifier: Modifier
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .then(doubleTapModifier)
    ) {
        AsyncImage(
            model = url,
            contentDescription = "Zoomable Image",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.Center)
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
                .transformable(state = transformableState)
        )
    }
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

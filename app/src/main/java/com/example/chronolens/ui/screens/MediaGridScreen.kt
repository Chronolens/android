package com.example.chronolens.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.size.Scale
import com.example.chronolens.models.LocalMedia
import com.example.chronolens.models.MediaAsset
import com.example.chronolens.models.RemoteMedia
import com.example.chronolens.viewModels.MediaGridScreenViewModel
import com.example.chronolens.viewModels.MediaGridState

@Composable
fun MediaGridScreen(viewModel: MediaGridScreenViewModel,state: State<MediaGridState>,navController: NavController) {

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(state.value.media) { asset ->
            ImageItem(viewModel,asset) {
                viewModel.updateCurrentAsset(asset)
                navController.navigate("fullscreenMediaView")
            }
        }
    }
}

@Composable
fun ImageItem(viewModel: MediaGridScreenViewModel, mediaAsset: MediaAsset, onClick:(MediaAsset) -> Unit) {
    Box(
        modifier = Modifier
            .padding(2.dp)
            .fillMaxWidth()
            .aspectRatio(1f), // Maintain a square aspect ratio
        contentAlignment = Alignment.Center,
    ) {

        if (mediaAsset is LocalMedia) {
            val localAsset: LocalMedia = mediaAsset
            // State to hold the bitmap
            //var bitmap by remember { mutableStateOf<Bitmap?>(null) }
            // Load the bitmap asynchronously in a coroutine
            val context = LocalContext.current
            val model = ImageRequest.Builder(context)
                .data(localAsset.path)
                .size(120) // Load a smaller size
                .scale(Scale.FILL) // Adjust the scaling if needed
                .build()
            Box {
                AsyncImage(
                    model = model,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable{
                            onClick(mediaAsset)
                        },

                )
                if (localAsset.remoteId != null) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.Blue,
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .align(Alignment.TopEnd)
                    )
                }
            }
        } else if (mediaAsset is RemoteMedia) {
            val remoteAsset: RemoteMedia = mediaAsset
            var imageUrl by remember { mutableStateOf<String?>(null) }
            // Fetch the URL asynchronously when the component is first drawn
            LaunchedEffect(mediaAsset) {
                val url = viewModel.getRemoteAssetPreviewUrl(remoteAsset.id)
                imageUrl = url // Set the state once the URL is fetched
            }
            Box {
                if (imageUrl != null) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable{
                                onClick(mediaAsset)
                            },
                    )
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = Color.Blue,
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .align(Alignment.TopEnd)
                    )
                } else {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}

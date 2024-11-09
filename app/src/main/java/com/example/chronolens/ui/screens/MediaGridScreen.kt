package com.example.chronolens.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
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

@Composable
fun MediaGridScreen(
    viewModel: MediaGridScreenViewModel,
    state: State<MediaGridState>,
    navController: NavController,
    work: WorkManagerViewModel,
    modifier: Modifier
) {

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .then(modifier),
    ) {
        items(state.value.media) { asset ->
            ImageItem(viewModel,asset) {
                viewModel.updateCurrentAsset(asset)
                navController.navigate(ChronolensNav.FullScreenMedia.name) {
                    popUpTo(ChronolensNav.FullScreenMedia.name) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }
}

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
            val localAsset: LocalMedia = mediaAsset
            // State to hold the bitmap
            //var bitmap by remember { mutableStateOf<Bitmap?>(null) }
            // Load the bitmap asynchronously in a coroutine
            val context = LocalContext.current
            val model = ImageRequest.Builder(context)
                .data(localAsset.path)
                .size(120)
                .scale(Scale.FILL)
                .build()
            Box {
                AsyncImage(
                    model = model,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable {
                            onClick(mediaAsset)
                        },

                    )
                if (localAsset.remoteId != null) {
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

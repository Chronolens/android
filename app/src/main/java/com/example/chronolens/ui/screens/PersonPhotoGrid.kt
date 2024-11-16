package com.example.chronolens.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.example.chronolens.R
import com.example.chronolens.models.KnownPerson
import com.example.chronolens.models.RemoteMedia
import com.example.chronolens.utils.ChronolensNav
import com.example.chronolens.viewModels.MediaGridScreenViewModel
import com.example.chronolens.viewModels.PersonPhotoGridState

@Composable
fun PersonPhotoGrid(
    viewModel: MediaGridScreenViewModel,
    personPhotoGridState: State<PersonPhotoGridState>,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val personPhotoGridStateValue = personPhotoGridState.value

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Display person's name or "Unknown Person"
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = if (personPhotoGridStateValue.person is KnownPerson) {
                    personPhotoGridStateValue.person.name
                } else {
                    "Unknown Person"
                },
                style = MaterialTheme.typography.titleLarge
            )
        }

        // Create a grid for displaying photos
        LazyVerticalGrid(
            columns = GridCells.Fixed(4), // Adjust the number of columns as needed
            modifier = Modifier.fillMaxSize(),
        ) {
            items(personPhotoGridStateValue.photos) { remoteMedia ->
                RemoteMediaItem(
                    viewModel = viewModel,
                    mediaAsset = remoteMedia,
                    onClick = {
                        viewModel.updateCurrentAsset(remoteMedia)
                        navController.navigate(ChronolensNav.FullScreenMedia.name) {
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun RemoteMediaItem(
    viewModel: MediaGridScreenViewModel,
    mediaAsset: RemoteMedia,
    onClick: (RemoteMedia) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    var imageUrl by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .padding(1.dp)
            .fillMaxWidth()
            .aspectRatio(1f),
        contentAlignment = Alignment.Center,
    ){
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
                    .clickable { onClick(mediaAsset) },
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

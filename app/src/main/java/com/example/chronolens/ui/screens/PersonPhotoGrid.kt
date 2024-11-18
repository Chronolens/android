package com.example.chronolens.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.example.chronolens.models.KnownPerson
import com.example.chronolens.utils.ChronolensNav
import com.example.chronolens.viewModels.MediaGridScreenViewModel
import com.example.chronolens.viewModels.PersonPhotoGridState
import kotlinx.coroutines.flow.StateFlow

@Composable
fun PersonPhotoGrid(
    viewModel: MediaGridScreenViewModel,
    personPhotoGridState: StateFlow<PersonPhotoGridState>,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val personPhotoGridStateValue by personPhotoGridState.collectAsState()
    val person = personPhotoGridStateValue.person

    val clusterId = person!!.personId

    Column(
        modifier = modifier.fillMaxSize()
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = if (person is KnownPerson) {
                    person.name
                } else {
                    "Unknown Person"
                },
                style = MaterialTheme.typography.titleLarge
            )
        }


        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(personPhotoGridStateValue.photos) { photoPreview ->
                RemoteMediaItem(
                    mediaAsset = photoPreview,
                    onClick = {
                        viewModel.updateCurrentAssetHelper(photoPreview)
                        navController.navigate(ChronolensNav.FullScreenMedia.name) {
                            launchSingleTop = true
                        }
                    }
                )
            }


            if (personPhotoGridStateValue.isLoading) {
                item {
                    CircularProgressIndicator(Modifier.padding(16.dp))
                }
            } else if (personPhotoGridStateValue.hasMore) {
                item {
                    LaunchedEffect(Unit) {
                        viewModel.loadNextPage(clusterId)
                    }
                }
            }
        }
    }
}

@Composable
fun RemoteMediaItem(
    mediaAsset: Map<String, String>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val imageUrl = mediaAsset["preview_link"] ?: ""

    Box(
        modifier = modifier
            .padding(1.dp)
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(1f),
            contentScale = ContentScale.Crop
        )
    }
}
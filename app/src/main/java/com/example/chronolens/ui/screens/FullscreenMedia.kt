package com.example.chronolens.ui.screens

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
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top buttons for return and bookmark/save/favorite
        Row(
            modifier = Modifier
                .padding(top = 20.dp)
                .padding(horizontal = 20.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                navController.navigateUp()
            }) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            IconButton(onClick = {
                println("Bookmark button pressed")
            }) {
                Icon(imageVector = Icons.Default.FavoriteBorder, contentDescription = "Bookmark")
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            contentAlignment = Alignment.Center
        ) {
            LoadFullImage(mediaAsset!!, viewModel)
        }


        Spacer(modifier = Modifier.weight(1f))

        // Bottom buttons for metadata, share, cloud (upload/remove toggle), and delete/transfer
        Row(
            modifier = Modifier
                .padding(bottom = 40.dp)
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = {
                println("Menu button pressed")
            }) {
                Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu")
            }
            IconButton(onClick = {
                println("Share button pressed")
            }) {
                Icon(imageVector = Icons.Default.Share, contentDescription = "Share")
            }
            CloudIcon(mediaAsset!!,viewModel)
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
    if (mediaAsset is RemoteMedia) {
        var imageUrl by remember { mutableStateOf<String?>(null) }
        LaunchedEffect(mediaAsset) {
            val url = viewModel.getRemoteAssetFullImageUrl(mediaAsset.id)
            imageUrl = url
        }
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Image",
            )
        } else {
            CircularProgressIndicator()
        }
    } else if (mediaAsset is LocalMedia) {
        AsyncImage(
            model = mediaAsset.path,
            contentDescription = "Image",
        )
    }
}

@Composable
fun CloudIcon(asset: MediaAsset,viewModel: MediaGridScreenViewModel){

    if (asset is LocalMedia) {
        if (asset.remoteId != null) {
            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Uploaded")
        } else {
            IconButton(onClick = {
                println("Uploading local asset: ${asset.path}")
                viewModel.uploadMedia(asset)
            }) {
                Icon(imageVector = Icons.Default.KeyboardArrowUp, contentDescription = "Upload")
            }
        }
    } else if (asset is RemoteMedia) {
        IconButton(onClick = {
            println("Remove from cloud not implemented yet")
        }) {
            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Cloud")
        }
    }
}

@Composable
fun DeleteOrTransferIcon(asset: MediaAsset) {
    if (asset is RemoteMedia) {
        IconButton(onClick = {
            println("Downloading not implemented yet")
        }) {
            Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = "Download")
        }
    } else if (asset is LocalMedia) {
        IconButton(onClick = {
            println("Deleting not implemented yet")
        }) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
        }
    }
}
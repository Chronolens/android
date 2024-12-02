package com.example.chronolens.ui.components

import android.util.Log
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.chronolens.R
import com.example.chronolens.models.LocalMedia
import com.example.chronolens.models.MediaAsset
import com.example.chronolens.models.RemoteMedia
import com.example.chronolens.utils.shareImages
import com.example.chronolens.viewModels.DownloadingState
import com.example.chronolens.viewModels.FullscreenImageState
import com.example.chronolens.viewModels.MediaGridViewModel


@Composable
fun DeleteOrDownloadButton(
    asset: MediaAsset,
    viewModel: MediaGridViewModel,
    state: State<FullscreenImageState>
) {

    val context = LocalContext.current
    if (asset is RemoteMedia) {
        IconButton(
            onClick = {
                viewModel.downloadSingle(asset, context)
            },
            enabled = state.value.downloadState == null
        ) {
            when (state.value.downloadState) {
                DownloadingState.Downloading -> {
                    CircularProgressIndicator()
                }
                DownloadingState.Downloaded -> {
                    Icon(
                        imageVector = Icons.Default.Done, // TODO: change icon?
                        contentDescription = "Download",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                null -> {
                    Icon(
                        painter = painterResource(id = R.drawable.downloadsimple),
                        contentDescription = "Download",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

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


@Composable
fun ShareButton(mediaAsset: LocalMedia) {
    val context = LocalContext.current
    IconButton(onClick = {
        shareImages(context, listOf(mediaAsset))
    }) {
        Icon(
            imageVector = Icons.Default.Share,
            contentDescription = "Share",
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}


@Composable
fun UploadOrRemoveButton(
    asset: MediaAsset,
    viewModel: MediaGridViewModel,
    state: State<FullscreenImageState>
) {
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
            // TODO: maybe find another icon for loading
            IconButton(
                enabled = !state.value.uploading,
                onClick = {
                    Log.i("UploadOrRemove", "Uploading local asset: ${asset.path}")
                    viewModel.uploadSingle(asset)
                }
            ) {
                if (state.value.uploading) {
                    CircularProgressIndicator()
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.uploadsimple),
                        contentDescription = "Upload",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
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
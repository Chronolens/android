package com.example.chronolens.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
fun RemoteMediaItem(
    mediaAsset: Map<String, String>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val imageUrl = mediaAsset["preview_url"] ?: ""

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

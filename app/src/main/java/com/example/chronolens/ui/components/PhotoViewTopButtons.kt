package com.example.chronolens.ui.components

import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController


@Composable
fun BackButton(navController: NavHostController) {
    IconButton(onClick = { navController.navigateUp() }) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = Color.White
        )
    }
}


@Composable
fun BookmarkButton() {
    IconButton(onClick = { Log.i("FullscreenMediaView", "Bookmark button pressed") }) {
        Icon(
            imageVector = Icons.Default.FavoriteBorder,
            contentDescription = "Bookmark",
            tint = Color.White
        )
    }
}


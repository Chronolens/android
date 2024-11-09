package com.example.chronolens.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun SettingsScreen(modifier: Modifier) {
    Box(modifier = modifier.fillMaxSize()){
        Text("SettingsScreen", modifier = Modifier.align(Alignment.Center))
    }
}

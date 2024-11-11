package com.example.chronolens.models

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.chronolens.utils.ChronolensNav

data class SettingsItem(
    @StringRes val title: Int,
    @StringRes val description: Int,
    val icon: ImageVector,
    val nav: ChronolensNav
)
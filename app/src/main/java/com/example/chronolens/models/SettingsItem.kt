package com.example.chronolens.models

import androidx.annotation.StringRes
import com.example.chronolens.utils.ChronolensNav

data class SettingsItem(
    @StringRes val title: Int,
    @StringRes val description: Int,
    val icon: Int,
    val nav: ChronolensNav
)
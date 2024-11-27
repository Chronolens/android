package com.example.chronolens.models

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.chronolens.utils.ChronolensNav

data class SettingsItem(
    @StringRes val title: Int,
    @StringRes val description: Int,
    @DrawableRes val icon: Int,
    val nav: ChronolensNav
)
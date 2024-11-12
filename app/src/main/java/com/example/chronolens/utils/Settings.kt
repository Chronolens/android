package com.example.chronolens.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Done
import com.example.chronolens.R
import com.example.chronolens.models.SettingsItem

object Settings {

    val options = listOf(
        SettingsItem(
            title = R.string.settings_background_upload_title,
            description = R.string.settings_background_upload_desc,
            icon = R.drawable.upload,
            nav = ChronolensNav.BackgroundUpload
        ),
        SettingsItem(
            title = R.string.settings_machine_learning_title,
            description = R.string.settings_machine_learning_desc,
            icon = R.drawable.harddrives,
            nav = ChronolensNav.MachineLearning
        ),
        SettingsItem(
            title = R.string.settings_activity_history_title,
            description = R.string.settings_activity_history_desc,
            icon = R.drawable.list,
            nav = ChronolensNav.ActivityHistory
        )
    )

}
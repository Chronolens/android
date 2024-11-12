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
            icon = Icons.Outlined.Done,
            nav = ChronolensNav.BackgroundUpload
        ),
        SettingsItem(
            title = R.string.settings_machine_learning_title,
            description = R.string.settings_machine_learning_desc,
            icon = Icons.Outlined.Done,
            nav = ChronolensNav.MachineLearning
        ),
        SettingsItem(
            title = R.string.settings_activity_history_title,
            description = R.string.settings_activity_history_desc,
            icon = Icons.Outlined.Done,
            nav = ChronolensNav.ActivityHistory
        )
    )

}
package com.example.chronolens.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.chronolens.R
import com.example.chronolens.viewModels.WorkManagerViewModel

@Composable
fun BackgroundUploadScreen(
    modifier: Modifier = Modifier,
    workManager: WorkManagerViewModel
) {
    var requireWifi by remember { mutableStateOf(true) }
    var requireCharging by remember { mutableStateOf(false) }
    var since by remember { mutableStateOf(null) }
    var includeVideos by remember { mutableStateOf(false) }
    var period by remember { mutableLongStateOf(15) } // minimum is 15 minutes
    val periodOptions = listOf(15, 30, 45, 60, 90)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp)
    ) {
        item {
            BackgroundUploadOption(
                icon = Icons.Outlined.Check,
                title = stringResource(R.string.background_uploads_now),
                description = stringResource(R.string.background_uploads_now_desc),
                onClick = { workManager.oneTimeBackgroundSync() }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            BackgroundUploadOption(
                icon = Icons.Outlined.Check,
                title = stringResource(R.string.background_uploads_periodic),
                description = stringResource(R.string.background_uploads_periodic_desc),
                onClick = {
                    workManager.periodicBackgroundSync(
                        period,
                        requireWifi,
                        requireCharging,
                        0,
                        includeVideos,
                        false
                    )
                },
            )

            DropdownMenuPicker("period", periodOptions, period.toInt()) { period = it.toLong() }
            ToggleOptionRow("Wi-Fi only?", requireWifi) { requireWifi = it }
            ToggleOptionRow("Requires Charging?", requireCharging) { requireCharging = it }
            SettingsOptionRow("Upload Since", since ?: "ALL")
            ToggleOptionRow("Include Videos?", includeVideos) { includeVideos = it }
        }
    }
}


@Composable
fun DropdownMenuPicker(
    label: String,
    options: List<Int>,
    selectedOption: Int,
    onOptionSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(label)
        Box {
            Text(
                text = formatTime(selectedOption),
                modifier = Modifier
                    .padding(8.dp)
                    .clickable { expanded = true }
                    .padding(8.dp)
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        },
                        text = { Text(formatTime(option)) }

                    )
                }
            }
        }
    }
}

@Composable
fun BackgroundUploadOption(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.padding(end = 8.dp)
        )
        Column {
            Text(title)
            Text(description)
        }
    }
}

@Composable
fun SettingsOptionRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label)
        Spacer(modifier = Modifier.weight(1f))
        Text(value)
    }
}

@Composable
fun ToggleOptionRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label)
        Spacer(modifier = Modifier.weight(1f))
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

// TODO: use string values from the xml
private fun formatTime(minutes: Int): String {

    val h = minutes / 60
    val m = minutes % 60

    var formatedTime = ""
    if (h != 0) formatedTime+="$h h "
    if (m != 0) formatedTime+="$m min"

    return formatedTime
}

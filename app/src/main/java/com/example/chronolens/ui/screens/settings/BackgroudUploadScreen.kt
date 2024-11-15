package com.example.chronolens.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
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
import androidx.work.WorkInfo
import com.example.chronolens.R
import com.example.chronolens.ui.components.AlertConfirmDialog
import com.example.chronolens.viewModels.WorkManagerViewModel

@Composable
fun BackgroundUploadScreen(
    modifier: Modifier = Modifier,
    workManager: WorkManagerViewModel,
    isLoading: State<Boolean>,
    workInfo: State<WorkInfo.State?>

) {
    var requireWifi by remember { mutableStateOf(true) }
    var requireCharging by remember { mutableStateOf(false) }
    var since by remember { mutableStateOf(null) }
    var includeVideos by remember { mutableStateOf(false) }
    var period by remember { mutableLongStateOf(15) } // minimum is 15 minutes
    val periodOptions = listOf(15, 30, 45, 60, 90, 120, 240, 480, 1440)

    val uploadNowVisible = remember { mutableStateOf(false) }
    val periodicUploadVisible = remember { mutableStateOf(false) }

    if (uploadNowVisible.value) {
        AlertConfirmDialog(
            title = "Upload everything now",
            text = "Are you sure bro?",
            confirmOption = workManager::oneTimeBackgroundSync,
            visible = uploadNowVisible
        )
    }

    if (periodicUploadVisible.value) {
        AlertConfirmDialog(
            title = "Periodic Upload",
            text = "Are you sure bro?",
            confirmOption = {
                workManager.periodicBackgroundSync(
                    period = period,
                    requireWifi = requireWifi,
                    requireCharging = requireCharging,
                    since = 0,
                    includeVideos = includeVideos,
                    startNow = false
                )
            },
            visible = periodicUploadVisible
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp)
    ) {
        item {
            Column {
                Text(text = workInfo.value.toString())
                BackgroundUploadOption(
                    icon = Icons.Outlined.Check,
                    title = stringResource(R.string.background_uploads_now),
                    description = stringResource(R.string.background_uploads_now_desc),
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    if (isLoading.value) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = workManager::cancelOneTimeBackgroundSync) {
                            Text("Cancel")
                        }
                    } else {
                        Button(
                            onClick = { uploadNowVisible.value = true }
                        ) {
                            Text("Start")
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            BackgroundUploadOption(
                icon = Icons.Outlined.Check,
                title = stringResource(R.string.background_uploads_periodic),
                description = stringResource(R.string.background_uploads_periodic_desc),

                )

            DropdownMenuPicker("period", periodOptions, period.toInt()) { period = it.toLong() }
            ToggleOptionRow("Wi-Fi only?", requireWifi) { requireWifi = it }
            ToggleOptionRow("Requires Charging?", requireCharging) { requireCharging = it }
            SettingsOptionRow("Upload Since", since ?: "ALL")
            ToggleOptionRow("Include Videos?", includeVideos) { includeVideos = it }
            Button(onClick = { periodicUploadVisible.value = true }) {
                Text(stringResource(R.string.start))
            }
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

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label)
        Spacer(modifier = Modifier.weight(1f))
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
    if (h != 0) formatedTime += "$h h "
    if (m != 0) formatedTime += "$m min"

    return formatedTime
}

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
import androidx.compose.runtime.MutableState
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
import com.example.chronolens.viewModels.WorkManagerState
import com.example.chronolens.viewModels.WorkManagerViewModel
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun BackgroundUploadScreen(
    modifier: Modifier = Modifier,
    workManager: WorkManagerViewModel,
    workManagerState: State<WorkManagerState>

) {
    var requireWifi by remember { mutableStateOf(true) }
    var requireCharging by remember { mutableStateOf(false) }
    var requireBatteryNotLow by remember { mutableStateOf(true) }
    var includeVideos by remember { mutableStateOf(false) }
    var period by remember { mutableLongStateOf(15) } // minimum is 15 minutes
    val periodOptions: List<Long> = listOf(15, 30, 45, 60, 90, 120, 240, 480, 1440)

    val uploadNowVisible = remember { mutableStateOf(false) }
    val periodicUploadVisible = remember { mutableStateOf(false) }

    if (uploadNowVisible.value) {
        AlertConfirmDialog(
            title = stringResource(R.string.upload_now),
            text = stringResource(R.string.upload_now_desc),
            confirmOption = workManager::oneTimeBackgroundSync,
            visible = uploadNowVisible
        )
    }

    if (periodicUploadVisible.value) {
        AlertConfirmDialog(
            title = stringResource(R.string.upload_periodic),
            text = stringResource(R.string.upload_periodic_desc, formatTime(period)),
            confirmOption = {
                workManager.periodicBackgroundSync(
                    period = period,
                    requireWifi = requireWifi,
                    requireCharging = requireCharging,
                    requireBatteryNotLow = requireBatteryNotLow,
                    includeVideos = includeVideos
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
                Text(text = workManagerState.value.oneTimeWorkInfoState.toString())
                BackgroundUploadOption(
                    icon = Icons.Outlined.Check,
                    title = stringResource(R.string.background_uploads_now),
                    description = stringResource(R.string.background_uploads_now_desc),
                )
                StartCancelOneTimeRow(
                    state = workManagerState,
                    visible = uploadNowVisible,
                    cancel = workManager::cancelOneTimeBackgroundSync
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            BackgroundUploadOption(
                icon = Icons.Outlined.Check,
                title = stringResource(R.string.background_uploads_periodic),
                description = stringResource(R.string.background_uploads_periodic_desc),
            )

            DropdownMenuPicker(
                label = stringResource(R.string.period),
                options = periodOptions,
                selectedOption = period,
                onOptionSelected = { period = it }
            )
            ToggleOptionRow(
                label = stringResource(R.string.require_wifi),
                checked = requireWifi,
                onCheckedChange = { requireWifi = it }
            )

            ToggleOptionRow(
                label = stringResource(R.string.require_charging),
                checked = requireCharging,
                onCheckedChange = { requireCharging = it }
            )
            ToggleOptionRow(
                label = stringResource(R.string.require_battery_not_low),
                checked = requireBatteryNotLow,
                onCheckedChange = { requireBatteryNotLow = it }
            )
            ToggleOptionRow(
                label = stringResource(R.string.include_videos),
                checked = includeVideos,
                onCheckedChange = { includeVideos = it }
            )
            Text(text = workManagerState.value.periodicWorkInfoState.toString())
            if (workManagerState.value.nextJob != null) {
                // get locale for this
                val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
                val dateString: String = formatter.format(Date(workManagerState.value.nextJob!!))
                Text(text = dateString)
            } else {
                Text("No scheduled jobs")
            }
            StartCancelPeriodicRow(
                state = workManagerState,
                visible = periodicUploadVisible,
                cancel = workManager::cancelPeriodicBackgroundSync
            )
        }
    }
}

@Composable
fun StartCancelOneTimeRow(
    state: State<WorkManagerState>,
    cancel: () -> Unit,
    visible: MutableState<Boolean>

) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        if (state.value.isLoading) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = cancel) {
                Text(stringResource(R.string.cancel))
            }
        } else {
            Button(
                onClick = { visible.value = true }
            ) {
                Text(stringResource(R.string.start))
            }
        }
    }
}

@Composable
fun StartCancelPeriodicRow(
    state: State<WorkManagerState>,
    cancel: () -> Unit,
    visible: MutableState<Boolean>

) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {

        if (state.value.isReady) {
            // Start
            Button(
                onClick = { visible.value = true }
            ) {
                Text(stringResource(R.string.start))
            }
        } else {
            Button(onClick = cancel) {
                Text(stringResource(R.string.cancel))
            }
        }
    }
}


@Composable
fun DropdownMenuPicker(
    label: String,
    options: List<Long>,
    selectedOption: Long,
    onOptionSelected: (Long) -> Unit
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

@Composable
private fun formatTime(minutes: Long): String {

    val h = minutes / 60
    val m = minutes % 60

    var formatedTime = ""
    if (h != 0L) formatedTime += stringResource(R.string.formatted_hour, h)
    if (m != 0L) formatedTime += stringResource(R.string.formatted_minute, m)

    return formatedTime
}

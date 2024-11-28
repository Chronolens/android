package com.example.chronolens.ui.screens.settings

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.chronolens.R
import com.example.chronolens.ui.components.AlertConfirmDialog
import com.example.chronolens.ui.theme.defaultButtonColors
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
    var period by remember { mutableLongStateOf(15) }
    val periodOptions: List<Long> = listOf(15, 30, 45, 60, 90, 120, 240, 480, 1440)

    val uploadNowVisible = remember { mutableStateOf(false) }
    val periodicUploadVisible = remember { mutableStateOf(false) }

    if (uploadNowVisible.value) {
        CustomAlertConfirmDialog(
            title = stringResource(R.string.upload_now),
            text = stringResource(R.string.upload_now_desc),
            confirmOption = workManager::oneTimeBackgroundSync,
            visible = uploadNowVisible
        )
    }

    if (periodicUploadVisible.value) {
        CustomAlertConfirmDialog(
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
        // Upload now
        item {
            Column {
                BackgroundUploadOption(
                    iconRes = R.drawable.upload,
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
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 10.dp),
                color = Color.White
            )
        }

        // Periodic upload
        item {
            BackgroundUploadOption(
                iconRes = R.drawable.clockclockwise,
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
            if (workManagerState.value.nextJob != null) {
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
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = state.value.oneTimeWorkInfoState?.toString() ?: stringResource(R.string.no_queued_job),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.align(Alignment.CenterVertically)
        )

        if (state.value.isLoading) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = cancel,
                colors = defaultButtonColors(),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(stringResource(R.string.cancel))
            }
        } else {
            Button(
                onClick = { visible.value = true },
                colors = defaultButtonColors(),
                shape = RoundedCornerShape(4.dp)
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
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = state.value.periodicWorkInfoState?.toString() ?: stringResource(R.string.no_queued_job),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.align(Alignment.CenterVertically)
        )

        if (state.value.isReady) {
            Button(
                onClick = { visible.value = true },
                colors = defaultButtonColors(),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(stringResource(R.string.start))
            }
        } else {
            Button(
                onClick = cancel,
                colors = defaultButtonColors(),
                shape = RoundedCornerShape(4.dp)
            ) {
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
    @DrawableRes iconRes: Int,
    title: String,
    description: String,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.padding(end = 16.dp)
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiary
            )
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
            onCheckedChange = onCheckedChange,
            colors = CheckboxColors(
                checkedCheckmarkColor = MaterialTheme.colorScheme.background,
                checkedBoxColor = MaterialTheme.colorScheme.tertiary,
                checkedBorderColor = MaterialTheme.colorScheme.tertiary,
                uncheckedCheckmarkColor = MaterialTheme.colorScheme.background,
                uncheckedBoxColor = MaterialTheme.colorScheme.background,
                uncheckedBorderColor = MaterialTheme.colorScheme.primaryContainer,
                disabledCheckedBoxColor = MaterialTheme.colorScheme.background,
                disabledUncheckedBoxColor = MaterialTheme.colorScheme.background,
                disabledIndeterminateBoxColor = MaterialTheme.colorScheme.background,
                disabledBorderColor = MaterialTheme.colorScheme.background,
                disabledUncheckedBorderColor = MaterialTheme.colorScheme.background,
                disabledIndeterminateBorderColor = MaterialTheme.colorScheme.background
            )
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomAlertConfirmDialog(
    title: String,
    text: String,
    confirmOption: () -> Unit,
    visible: MutableState<Boolean>,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = MaterialTheme.colorScheme.secondary
) {
    BasicAlertDialog(
        onDismissRequest = { visible.value = false },
        modifier = Modifier
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(primaryColor, secondaryColor)
                ),
                shape = RoundedCornerShape(8.dp)
            ),
        properties = DialogProperties(),
        content = {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { visible.value = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = {
                            confirmOption()
                            visible.value = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
    )
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

package com.example.chronolens.ui.screens.settings

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.chronolens.R
import com.example.chronolens.viewModels.WorkManagerViewModel


@Composable
fun BackgroundUploadScreen(
    modifier: Modifier,
    workmanager: WorkManagerViewModel
) {

    var requireWifi by remember { mutableStateOf(true) }
    var requireCharging by remember { mutableStateOf(false) }
    var period by remember { mutableIntStateOf(15) } // minimum is 15 minutes
    var since by remember { mutableStateOf(null) }
    var includeVideos by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        workmanager.oneTimeBackgroundSync()
                        //cancelPeriodicBackgroundSync()
                    }, verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    tint = Color.White
                )
                Column {
                    Text(stringResource(R.string.background_uploads_now))
                    Text(stringResource(R.string.background_uploads_now_desc))
                }
            }
        }
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {},
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Column {
                        Text(stringResource(R.string.background_uploads_periodic))
                        Text(stringResource(R.string.background_uploads_periodic_desc))
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("period")
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("wifi?")
                    Spacer(modifier = Modifier
                        .weight(1f)
                        .border(2.dp, Color.Blue))
                    Checkbox(
                        checked = requireWifi,
                        onCheckedChange = { requireWifi = it }
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("charging?")
                    Spacer(modifier = Modifier
                        .weight(1f)
                        .border(2.dp, Color.Blue))
                    Checkbox(
                        checked = requireCharging,
                        onCheckedChange = { requireCharging = it }
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("from now on or ALL or pick date")
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("include videos?")
                }

            }
        }
    }
}
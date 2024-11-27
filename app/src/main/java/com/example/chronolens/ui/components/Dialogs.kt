package com.example.chronolens.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Checkbox
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.chronolens.R

@Composable
fun AlertConfirmDialog(
    title: String,
    text: String,
    icon: ImageVector = Icons.Filled.Warning,
    confirmOption: () -> Unit,
    visible: MutableState<Boolean>
) {

    Dialog(onDismissRequest = { visible.value = false }) {
        Card {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(15.dp)
            ) {
                Spacer(modifier = Modifier.height(5.dp))
                Icon(imageVector = icon, contentDescription = null)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.h5,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(text = text, style = MaterialTheme.typography.h6)
                Spacer(modifier = Modifier.height(20.dp))
                Row {
                    Spacer(modifier = Modifier.weight(0.5f))
                    TextButton(onClick = { visible.value = false }) {
                        Text(text = stringResource(R.string.cancel), fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(
                        onClick = {
                            visible.value = false
                            confirmOption()
                        }
                    ) {
                        Text(text = stringResource(R.string.confirm), fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.weight(0.5f))
                }
            }

        }
    }

}


@Composable
fun AlbumsPickerDialog(
    visible: MutableState<Boolean>,
    confirmOption: (selectedAlbums: List<String>) -> Unit,
    title: String,
    albums: List<String>
) {
    val selectedAlbums = remember { mutableStateMapOf<String, Boolean>() }
    val allSelected = remember { mutableStateOf(false) }

    LaunchedEffect(albums) {
        albums.forEach { album ->
            if (!selectedAlbums.containsKey(album)) {
                selectedAlbums[album] = false
            }
        }
    }

    Dialog(onDismissRequest = { }) {
        Card {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(15.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.h5,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                ) {
                    // TODO: keep or take out select all?
                    item {
                        AlbumItem(
                            albumName = stringResource(R.string.select_all),
                            isSelected = allSelected.value,
                            onSelectionChange = { change ->
                                allSelected.value = change
                                val updatedSelection = selectedAlbums.toMutableMap().apply {
                                    keys.forEach { this[it] = change }
                                }
                                selectedAlbums.clear()
                                selectedAlbums.putAll(updatedSelection)
                            }
                        )
                    }
                    items(albums) { albumName ->
                        AlbumItem(
                            albumName = albumName,
                            isSelected = selectedAlbums[albumName] ?: false,
                            onSelectionChange = { isSelected ->
                                selectedAlbums[albumName] = isSelected
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(
                        onClick = {
                            visible.value = false
                            val selected = selectedAlbums.filter { it.value }.keys.toList()
                            confirmOption(selected)
                        }
                    ) {
                        Text(text = stringResource(R.string.confirm), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun AlbumItem(
    albumName: String,
    isSelected: Boolean,
    onSelectionChange: (Boolean) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onSelectionChange(it) }
        )
        Spacer(modifier = Modifier.padding(horizontal = 10.dp))
        Text(albumName)
    }
}

@SuppressLint("UnrememberedMutableState")
@Preview(showSystemUi = true)
@Composable
fun Prev() {
    Box(modifier = Modifier.fillMaxSize()) {
        AlbumsPickerDialog(
            title = "Upload All Media Now",
            albums = listOf("Chronolens", "Download", "Pictures", "Other"),
            confirmOption = {},
            visible = mutableStateOf(true)
        )
    }
}

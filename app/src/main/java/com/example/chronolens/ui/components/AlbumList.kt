package com.example.chronolens.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.chronolens.R

@Composable
fun AlbumColum(
    selectedAlbums: SnapshotStateMap<String, Boolean>,
    allSelected: MutableState<Boolean>,
    albums: List<String>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
    ) {
        // TODO: keep or take out select all?
        item {
            AlbumItem(
                albumName = stringResource(R.string.select_all),
                isSelected = allSelected.value && selectedAlbums.none { !it.value },
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
}

@Composable
fun AlbumItem(
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
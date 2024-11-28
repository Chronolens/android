package com.example.chronolens.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.MaterialTheme
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
fun AlbumColumn(
    selectedAlbums: SnapshotStateMap<String, Boolean>,
    allSelected: MutableState<Boolean>,
    albums: List<String>,
    modifier: Modifier
) {
    LazyColumn(
        modifier = modifier
    ) {
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
            onCheckedChange = { onSelectionChange(it) },
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
        Spacer(modifier = Modifier.padding(horizontal = 10.dp))
        Text(albumName)
    }
}
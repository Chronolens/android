package com.example.chronolens.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.chronolens.R
import com.example.chronolens.ui.components.AlbumColum
import com.example.chronolens.viewModels.MediaGridViewModel

@Composable
fun AlbumPickerScreen(modifier: Modifier, albums: List<String>, viewModel: MediaGridViewModel) {

    val selectedAlbums = remember { mutableStateMapOf<String, Boolean>() }
    val allSelected = remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(albums) {
        val userAlbums = viewModel.getUserAlbums() ?: listOf()
        albums.forEach { album ->
            if (!selectedAlbums.containsKey(album)) {
                selectedAlbums[album] = userAlbums.contains(album)
            }
        }
        allSelected.value = selectedAlbums.none { !it.value }
    }

    // TODO: pretty print
    Box(modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize()) {
            AlbumColum(
                selectedAlbums = selectedAlbums,
                allSelected = allSelected,
                albums = albums
            )
            Button(onClick = {
                viewModel.setAlbums(selectedAlbums.filter { it.value }.keys.toList())
                // TODO: keep or take out?
                Toast.makeText(
                    context,
                    context.resources.getString(R.string.albums_set),
                    Toast.LENGTH_LONG
                ).show()
                viewModel.refreshMediaGrid(context)
            }) {
                Text(stringResource(R.string.confirm))
            }
        }
    }
}
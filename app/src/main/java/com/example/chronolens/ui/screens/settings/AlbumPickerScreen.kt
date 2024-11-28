package com.example.chronolens.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.chronolens.R
import com.example.chronolens.ui.components.AlbumColumn
import com.example.chronolens.ui.theme.defaultButtonColors
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

    Box(modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize()) {
            AlbumColumn(
                selectedAlbums = selectedAlbums,
                allSelected = allSelected,
                albums = albums,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            )
//            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .weight(0.1f)
            ) {
                Button(
                    onClick = {
                        viewModel.setAlbums(selectedAlbums.filter { it.value }.keys.toList())

                        Toast.makeText(
                            context,
                            context.resources.getString(R.string.albums_set),
                            Toast.LENGTH_LONG
                        ).show()
                        viewModel.refreshMediaGrid(context)
                    },
                    colors = defaultButtonColors(),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(text = stringResource(R.string.confirm))
                }
            }
        }
    }
}
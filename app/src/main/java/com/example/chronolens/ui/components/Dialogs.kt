package com.example.chronolens.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
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
import com.example.chronolens.ui.theme.defaultButtonColors



// TODO : standardize deisgn with the other alerts
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
        allSelected.value = selectedAlbums.none { !it.value }
    }

    Dialog(onDismissRequest = { visible.value = false }) {
        Card(
            modifier = Modifier
                .wrapContentHeight() 
                .padding(horizontal = 20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.h5,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))

                AlbumColumn(
                    selectedAlbums = selectedAlbums,
                    allSelected = allSelected,
                    albums = albums,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                ) {
                    Button(
                        onClick = {
                            visible.value = false
                            val selected = selectedAlbums.filter { it.value }.keys.toList()
                            confirmOption(selected)
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
}



@SuppressLint("UnrememberedMutableState")
@Preview(showSystemUi = true)
@Composable
fun Prev() {
    Box(modifier = Modifier.fillMaxSize()) {
        AlbumsPickerDialog(
            title = "Upload All Media Now",
            albums = listOf("Chronolens", "Download", "Pictures", "Other"),//, "Screenshots", "Screenshots", "Screenshots", "Screenshots", "Screenshots", "Screenshots", "Screenshots", "Screenshots", "Screenshots", "Screenshots", "Screenshots", "Screenshots", "Screenshots"),
            confirmOption = {},
            visible = mutableStateOf(true)
        )
    }
}

//http://10.0.0.50:8090

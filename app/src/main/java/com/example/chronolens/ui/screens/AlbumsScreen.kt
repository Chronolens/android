package com.example.chronolens.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.chronolens.models.KnownPerson
import com.example.chronolens.models.Person
import com.example.chronolens.models.UnknownPerson
import com.example.chronolens.utils.ChronolensNav
import com.example.chronolens.viewModels.MediaGridScreenViewModel
import com.example.chronolens.viewModels.MediaGridState


// TODO: The current method is to get the people albums from the server at the start of mediagridstate
@Composable
fun AlbumsScreen(
    viewModel: MediaGridScreenViewModel,
    navController: NavController,
    state: State<MediaGridState>,
    modifier: Modifier = Modifier,
) {
    val selectedPeople = state.value.selectedPeople
    val showNameDialog by viewModel.showNameDialog.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            item {
                Text(
                    text = "People",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(state.value.people) { person ->
                        PersonItem(viewModel, person) {
                            viewModel.updateCurrentPersonPhotoGrid(it)
                            navController.navigate(ChronolensNav.PersonPhotoGrid.name)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        if (selectedPeople.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Selected: ${selectedPeople.size} unknown persons")
                Button(
                    onClick = {
                        viewModel.confirmPersonClustering()
                    }
                ) {
                    Text("Cluster")
                }
            }
        }

        if (showNameDialog) {
            NameInputDialog(
                onDismiss = { viewModel.dismissNameDialog() },
                onConfirm = { personName ->
                    viewModel.onNameConfirmed(personName)
                }
            )
        }
    }
}


@Composable
fun PersonItem(
    viewModel: MediaGridScreenViewModel,
    person: Person,
    onClick: (Person) -> Unit
) {
    val state by viewModel.mediaGridState.collectAsState()


    val isSelectable = person is UnknownPerson
    val isSelected = state.selectedPeople.containsKey(person.personId as Any)

    var isLongPressed by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        if (isLongPressed) {
                            isLongPressed = false
                        } else {
                            if (state.selectedPeople.isNotEmpty()) {

                                if (isSelectable) {

                                    viewModel.togglePersonSelection(person)
                                }
                            } else {

                                onClick(person)
                            }
                        }
                    },
                    onLongPress = {

                        if (isSelectable) {
                            isLongPressed = true
                            viewModel.togglePersonSelection(person)
                        }
                    }
                )
            }
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(RectangleShape)
                .border(
                    width = 4.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.secondary else Color.Transparent,
                    shape = RectangleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            person.photoBitmap?.let { bitmap ->
                Image(
                    painter = BitmapPainter(bitmap.asImageBitmap()),
                    contentDescription = "Person Photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } ?: Text("No Image")
        }

        if (person is KnownPerson) {
            Text(
                text = person.name,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}


// TODO: Fix for HEIF photos, as they are not being displayed probably due to the conversion to bitmap

@Composable
fun NameInputDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var personName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Name Unknown Persons") },
        text = {
            TextField(
                value = personName,
                onValueChange = { personName = it },
                label = { Text("Person Name") }
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (personName.isNotBlank()) {
                        onConfirm(personName)
                        onDismiss()
                    }
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


@Composable
fun FavoritesButton() {
    Button(
        onClick = { },
        modifier = Modifier.padding(8.dp)
    ) {
        Text("Favorites")
    }
}

@Composable
fun BinButton() {
    Button(
        onClick = { },
        modifier = Modifier.padding(8.dp)
    ) {
        Text("Bin")
    }
}


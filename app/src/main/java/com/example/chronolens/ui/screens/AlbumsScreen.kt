package com.example.chronolens.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
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
                onConfirm = { personName -> viewModel.onNameConfirmed(personName) },
                selectedPeopleCount = selectedPeople.size, 
                primaryColor = MaterialTheme.colorScheme.primary,
                secondaryColor = MaterialTheme.colorScheme.secondary
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NameInputDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    selectedPeopleCount: Int,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = MaterialTheme.colorScheme.secondary
) {
    var personName by remember { mutableStateOf("") }

    BasicAlertDialog(
        onDismissRequest = onDismiss,
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
                    text = if (selectedPeopleCount == 1) "Rename" else "Group People",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.4f), shape = RoundedCornerShape(4.dp))
                        .padding(12.dp)
                ) {
                    BasicTextField(
                        value = personName,
                        onValueChange = { personName = it },
                        textStyle = LocalTextStyle.current.copy(color = Color.White),
                        cursorBrush = SolidColor(Color.White),
                        decorationBox = { innerTextField ->
                            if (personName.isEmpty()) {
                                Text(
                                    text = "Person Name",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.5f)),
                                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                                )
                            }
                            innerTextField()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onDismiss,
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
                            if (personName.isNotBlank()) {
                                onConfirm(personName)
                                onDismiss()
                            }
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






// TODO: Fix for HEIF photos, as they are not being displayed probably due to the conversion to bitmap


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


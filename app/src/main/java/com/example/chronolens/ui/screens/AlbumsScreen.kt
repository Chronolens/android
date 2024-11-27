package com.example.chronolens.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.chronolens.models.KnownPerson
import com.example.chronolens.models.Person
import com.example.chronolens.utils.ChronolensNav
import com.example.chronolens.viewModels.MediaGridViewModel
import com.example.chronolens.viewModels.MediaGridState


// TODO: The current method is to get the people albums from the server at the start of mediagridstate
@Composable
fun AlbumsScreen(
    viewModel: MediaGridViewModel,
    navController: NavController,
    state: State<MediaGridState>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Favorites and Bin buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FavoritesButton()
                BinButton()
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // PEOPLE section
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

//        // System folders / albums section
//        item {
//            Text(
//                text = "System Folders",
//                style = MaterialTheme.typography.titleSmall,
//                modifier = Modifier.padding(horizontal = 8.dp)
//            )
//            LazyRow(
//                horizontalArrangement = Arrangement.spacedBy(8.dp),
//                contentPadding = PaddingValues(horizontal = 8.dp)
//            ) {
//                repeat(10) {
//                    item {
//                        Box(
//                            modifier = Modifier.size(88.dp),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            Text("aa")
//                        }
//                    }
//                }
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//        }
//
//        // Albums section - POSSIBLY REMOVE DUE TO LACK OF TIME
//        item {
//            Text(
//                text = "Albums",
//                style = MaterialTheme.typography.titleSmall,
//                modifier = Modifier.padding(horizontal = 8.dp)
//            )
//            LazyRow(
//                horizontalArrangement = Arrangement.spacedBy(8.dp),
//                contentPadding = PaddingValues(horizontal = 8.dp)
//            ) {
//                repeat(10) {
//                    item {
//                        Box(
//                            modifier = Modifier.size(88.dp),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            Text("aa")
//                        }
//                    }
//                }
//            }
//        }
    }
}



// TODO: Fix for HEIF photos, as they are not being displayed probably due to the conversion to bitmap
@Composable
fun PersonItem (
    viewModel: MediaGridViewModel,
    person: Person,
    onClick: (Person) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 4.dp).clickable { onClick(person) }

    ) {

        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(RectangleShape),
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


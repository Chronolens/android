package com.example.chronolens.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.chronolens.models.Person
import com.example.chronolens.viewModels.MediaGridScreenViewModel
import com.example.chronolens.viewModels.MediaGridState


// TODO: The current method is to get the people albums from the server at the start of mediagridstate

@Composable
fun AlbumsScreen(
    viewModel: MediaGridScreenViewModel,
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


        item {
            Text(
                text = "People",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp),
                modifier = Modifier.height(88.dp)
            ) {
                items(state.value.people) { person ->
                    PersonPhotoCard(person)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }





        item {
            Text(
                text = "System Folders",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                repeat(10) {
                    item {
                        Box(
                            modifier = Modifier.size(88.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("aa")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }



        item {
            Text(
                text = "Albums",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                repeat(10) {
                    item {
                        Box(
                            modifier = Modifier.size(88.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("aa")
                        }
                    }
                }
            }
        }

    }
}

@Composable
fun PersonPhotoCard(person: Person) {
    // Display the photo for both KnownPerson and UnknownPerson as a square image
    Box(
        modifier = Modifier
            .size(88.dp), // Ensures the box is square
        contentAlignment = Alignment.Center
    ) {
        // Use AsyncImage to load the image from the photoLink
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(person.photoLink)
                .crossfade(true)
                .build(),
            contentDescription = "Person Photo",
            modifier = Modifier
                .fillMaxSize() // Fills the Box
                .clip(RectangleShape), // Ensures image stays within the square bounds
            contentScale = ContentScale.Crop // Crop to fit the square
        )
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


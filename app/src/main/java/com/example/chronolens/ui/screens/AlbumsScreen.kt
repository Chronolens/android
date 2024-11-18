package com.example.chronolens.ui.screens

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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.chronolens.viewModels.MediaGridScreenViewModel


// TODO Implement and call the People request in the MediaGridScreenViewModel

@Composable
fun AlbumsScreen(
    viewModel: MediaGridScreenViewModel,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Favorite and Bin Buttons
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



        // People Row
        item {
            Text(
                text = "People",
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
            // System Folders Row
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
            // User-Created Albums Row
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


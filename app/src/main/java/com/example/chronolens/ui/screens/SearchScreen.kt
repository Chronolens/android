package com.example.chronolens.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.chronolens.R
import com.example.chronolens.ui.components.RemoteMediaItem
import com.example.chronolens.utils.ChronolensNav
import com.example.chronolens.viewModels.ClipSearchState
import com.example.chronolens.viewModels.MediaGridViewModel
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource


@Composable
fun SearchScreen(
    viewModel: MediaGridViewModel,
    clipSearchState: StateFlow<ClipSearchState>,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val clipSearchStateValue by clipSearchState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var debouncedQuery by remember { mutableStateOf("") }


    LaunchedEffect(searchQuery) {
        kotlinx.coroutines.delay(1000)
        debouncedQuery = searchQuery
    }


    LaunchedEffect(debouncedQuery) {
        if (debouncedQuery.isNotEmpty()) {
            viewModel.clearSearchResults()
            viewModel.clipSearchNextPage(debouncedQuery)
        }
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        TextField(
            value = searchQuery,
            onValueChange = { newQuery ->
                searchQuery = newQuery
            },
            placeholder = { Text(text = "Search...") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            singleLine = true,
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.magnifyingglass),
                    contentDescription = "Search Icon",
                    modifier = Modifier.padding(8.dp)
                )
            },
            trailingIcon = {

                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = {
                        viewModel.clearSearchResults()
                        searchQuery = ""
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.x),
                            contentDescription = "Clear Search Query"
                        )
                    }
                }
            }
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(clipSearchStateValue.photos) { photoPreview ->
                RemoteMediaItem(
                    mediaAsset = photoPreview,
                    onClick = {
                        viewModel.updateCurrentAssetHelper(photoPreview)
                        navController.navigate(ChronolensNav.FullScreenMedia.name) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            if (clipSearchStateValue.isLoading) {
                item {
                    CircularProgressIndicator(Modifier.padding(16.dp))
                }
            }

            if (clipSearchStateValue.hasMore && !clipSearchStateValue.isLoading) {
                item {
                    LaunchedEffect(Unit) {
                        viewModel.clipSearchNextPage(searchQuery)
                    }
                }
            }
        }
    }
}


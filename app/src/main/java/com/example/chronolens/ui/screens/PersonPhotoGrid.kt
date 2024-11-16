package com.example.chronolens.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.example.chronolens.models.KnownPerson
import com.example.chronolens.viewModels.MediaGridScreenViewModel
import com.example.chronolens.viewModels.PersonPhotoGridState

@Composable
fun PersonPhotoGrid(
    viewModel: MediaGridScreenViewModel,
    personPhotoGridState: State<PersonPhotoGridState>,
    navController: NavHostController,
    modifier: Modifier
) {

    val personPhotoGridStateValue = personPhotoGridState.value

    Column (
        modifier = modifier.fillMaxSize()
    ) {
        // this isthe box that will serve as a button to rewrite the person's name or
        // form a new person from a group of unknown people/clusters
        Box {

            if (personPhotoGridStateValue.person is KnownPerson) {
                Text(
                    text = personPhotoGridStateValue.person.name,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    text = "Unknown Person",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // And the grid will be below here just like in MediaGridScreen.kt


    }



}
package com.example.chronolens.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.example.chronolens.viewModels.MediaGridScreenViewModel
import com.example.chronolens.viewModels.PersonPhotoGridState

@Composable
fun PersonPhotoGrid(
    viewModel: MediaGridScreenViewModel,
    personPhotoGridState: State<PersonPhotoGridState>,
    navController: NavHostController,
    modifier: Modifier
) {
    // CREATE A GRID OF PHOTOS OF A PERSON, for now jjust display the string "aaa"
    Column {
        for (i in 0..10) {
            Text("aaa")
        }
    }


}
package com.example.chronolens.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.chronolens.R
import com.example.chronolens.models.SettingsItem
import com.example.chronolens.utils.ChronolensNav
import com.example.chronolens.utils.Settings
import com.example.chronolens.viewModels.UserState
import com.example.chronolens.viewModels.UserViewModel

@Composable
fun SettingsScreen(
    modifier: Modifier,
    state: State<UserState>,
    viewModel: UserViewModel,
    navController: NavController
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
    ) {
        item {
            Profile(state, viewModel, navController)
            Spacer(modifier = Modifier.height(24.dp))
        }
        itemsIndexed(Settings.options) { i, item ->

            SettingsListItem(
                setting = item,
                navController = navController
            )
            if (i < Settings.options.size - 1) {
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp, horizontal = 10.dp),
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun Profile(state: State<UserState>, viewModel: UserViewModel, navController: NavController) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.user),
            contentDescription = null,
            modifier = Modifier.padding(end = 16.dp)
        )
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = state.value.username,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = state.value.server.replace("https://", "").replace("http://", ""),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiary
            )
        }
        Button(
            onClick = {
                viewModel.logout()
                navController.navigate(ChronolensNav.Login.name) {
                    popUpTo(0) { inclusive = true }
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onTertiaryContainer,
                contentColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(stringResource(R.string.logout))
        }
    }
}

@Composable
fun SettingsListItem(
    setting: SettingsItem,
    navController: NavController
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate(setting.nav.name) }
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = setting.icon),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.padding(end = 16.dp)
        )
        Column {
            Text(
                text = stringResource(setting.title),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = stringResource(setting.description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiary
            )
        }
    }
}

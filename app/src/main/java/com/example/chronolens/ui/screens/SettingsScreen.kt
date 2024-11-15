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
import androidx.compose.material.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
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
import com.example.chronolens.utils.Settings
import com.example.chronolens.viewModels.UserState

@Composable
fun SettingsScreen(
    modifier: Modifier,
    state: State<UserState>,
    navController: NavController
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
        //.padding(horizontal = 10.dp)
    ) {
        item {
            Profile(state)
            Spacer(modifier = Modifier.height(24.dp))
        }
        itemsIndexed(Settings.options) { i, item ->

            SettingsListItem(
                setting = item,
                navController = navController
            )
            if (i < Settings.options.size - 1) {
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp, horizontal = 10.dp),
                    color = Color.White
                )
            }
        }
    }
}

// TODO: worth even having??
@Composable
fun Profile(state: State<UserState>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.user),
            contentDescription = null
        )
        Column {
            Text(state.value.username)
            Text(state.value.server)
        }
    }
}

@Composable
fun SettingsListItem(
    setting: SettingsItem,
    navController: NavController
) {
    ListItem(
        leadingContent = {
            Icon(
                painter = painterResource(id = setting.icon),
                contentDescription = null,
                tint = Color.White,
            )
        },
        headlineContent = {
            Text(
                stringResource(setting.title),
            )
        },
        supportingContent = {
            Text(
                stringResource(setting.description),
            )
        },
        modifier = Modifier.clickable {
            navController.navigate(setting.nav.name)
        }
    )
}



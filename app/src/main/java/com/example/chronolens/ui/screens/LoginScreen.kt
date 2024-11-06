package com.example.chronolens.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chronolens.R
import com.example.chronolens.viewModels.UserLoginState
import com.example.chronolens.viewModels.UserState
import com.example.chronolens.viewModels.UserViewModel

@Composable
fun LoginScreen(
    viewModel: UserViewModel,
    userState: State<UserState>,
    grantAccess: () -> Unit
) {

    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        when (userState.value.userLoginState) {
            UserLoginState.Loading -> DisplayLoading(stringResource(R.string.app_name))
            UserLoginState.LoggedIn -> grantAccess()
            else -> {
                LoginPrompt(viewModel, userState)
            }
        }
    }
}

@Composable
fun LoginPrompt(
    viewModel: UserViewModel,
    userState: State<UserState>
) {

    var server by remember { mutableStateOf(viewModel.getServer()) }
    var username by remember { mutableStateOf(viewModel.getUsername()) }
    var password by remember { mutableStateOf("") }

    Column {
        Text(
            text = stringResource(R.string.app_name),
            fontSize = 40.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.padding(10.dp))
        TextField(
            value = server,
            onValueChange = { server = it },
            label = { Text("Server") },
            isError = userState.value.userLoginState == UserLoginState.CredentialsWrong,
            singleLine = true,
            enabled = userState.value.userLoginState != UserLoginState.Loading,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Next
            )
        )
        Spacer(modifier = Modifier.padding(10.dp))
        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            isError = userState.value.userLoginState == UserLoginState.CredentialsWrong,
            singleLine = true,
            enabled = userState.value.userLoginState != UserLoginState.Loading,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
        )
        Spacer(modifier = Modifier.padding(10.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            enabled = userState.value.userLoginState != UserLoginState.Loading,
            isError = userState.value.userLoginState == UserLoginState.CredentialsWrong,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            )
        )
        Spacer(modifier = Modifier.padding(10.dp))
        Button(
            onClick = {
                viewModel.login(server, username, password)
                username = ""
                password = ""
            },
            enabled = userState.value.userLoginState != UserLoginState.Loading,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            if (userState.value.userLoginState == UserLoginState.Loading) {
                Icon(imageVector = Icons.Filled.Refresh, contentDescription = null)
            } else {
                Text("Login")
            }
//        if (userState.value.userLoginState == UserLoginState.Error) {
//            AlertDialogTemplate(
//                onDismiss = onDismiss,
//                dialogTitle = stringResource(R.string.loginErrorTitle),
//                dialogText = stringResource(R.string.loginErrorMessage)
//            )
//        }
        }
    }
}


@Composable
fun DisplayLoading(appName: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = appName,
            fontSize = 40.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        CircularProgressIndicator(
            modifier = Modifier.width(64.dp),
        )
    }
}
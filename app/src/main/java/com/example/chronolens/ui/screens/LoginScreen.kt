package com.example.chronolens.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.chronolens.R
import com.example.chronolens.ui.theme.defaultButtonColors
import com.example.chronolens.viewModels.UserLoginState
import com.example.chronolens.viewModels.UserState
import com.example.chronolens.viewModels.UserViewModel

@Composable
fun LoginScreen(
    viewModel: UserViewModel,
    userState: State<UserState>,
    grantAccess: () -> Unit,
    modifier: Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    val brush = with(LocalDensity.current) {
        Brush.linearGradient(
            colors = listOf(colorScheme.primary, colorScheme.secondary),
            start = Offset(0f, 0f),
            end = Offset(screenWidth.toPx(), screenHeight.toPx())
        )
    }

    val server = remember { mutableStateOf(viewModel.getServer()) }
    val username = remember { mutableStateOf(viewModel.getUsername()) }
    val password = remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush)
            .then(modifier)
    ) {
        when (userState.value.userLoginState) {
            UserLoginState.Loading -> DisplayLoading()
            UserLoginState.LoggedIn -> grantAccess()
            else -> {
                LoginPrompt(
                    viewModel = viewModel,
                    userState = userState,
                    server = server,
                    username = username,
                    password = password
                )
            }
        }
    }
}


@Composable
fun LoginPrompt(
    viewModel: UserViewModel,
    userState: State<UserState>,
    server: MutableState<String>,
    username: MutableState<String>,
    password: MutableState<String>
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Transparent)
            .padding(start = 16.dp, end = 16.dp)
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Image(
            painter = painterResource(id = R.drawable.large_logo),
            contentDescription = stringResource(R.string.app_name),
            modifier = Modifier.fillMaxWidth(0.7f)
        )

        Spacer(modifier = Modifier.height(12.dp))

        CustomTextField(
            value = server.value,
            onValueChange = { server.value = it },
            label = "Server",
            isError = userState.value.userLoginState == UserLoginState.CredentialsWrong,
            keyboardType = KeyboardType.Uri,
            imeAction = ImeAction.Next
        )

        Spacer(modifier = Modifier.height(12.dp))

        CustomTextField(
            value = username.value,
            onValueChange = { username.value = it },
            label = "Username",
            isError = userState.value.userLoginState == UserLoginState.CredentialsWrong,
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next
        )

        Spacer(modifier = Modifier.height(12.dp))

        CustomTextField(
            value = password.value,
            onValueChange = { password.value = it },
            label = "Password",
            isPassword = true,
            isError = userState.value.userLoginState == UserLoginState.CredentialsWrong,
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                viewModel.login(server.value, username.value, password.value)
                password.value = ""
            },
            enabled = userState.value.userLoginState != UserLoginState.Loading,
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .align(Alignment.CenterHorizontally),
            colors = defaultButtonColors()
        ) {

            Text(
                "Log In",
                color = colorScheme.secondaryContainer,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Spacer(modifier = Modifier.weight(1.5f))
    }
}


@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPassword: Boolean = false,
    isError: Boolean = false,
    keyboardType: KeyboardType,
    imeAction: ImeAction
) {
    val colorScheme = MaterialTheme.colorScheme
    val labelColor = colorScheme.onBackground.copy(alpha = 0.7f)
    val typography = MaterialTheme.typography

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {

        Text(
            text = label,
            color = labelColor,
            style = typography.labelSmall,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            textStyle = typography.bodyLarge.copy(color = colorScheme.onBackground),
            cursorBrush = SolidColor(colorScheme.onPrimary),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {

                    if (value.isEmpty()) {
                        Text(
                            text = label,
                            color = labelColor,
                            style = typography.bodyMedium
                        )
                    }
                    innerTextField()
                }
            }
        )

        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth(),
            thickness = 2.dp,
            color = if (isError) Color.Red else colorScheme.onBackground.copy(alpha = 0.7f)
        )
    }
}


@Composable
fun DisplayLoading() {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .padding(bottom = 200.dp)
    ) {

        Image(
            painter = painterResource(id = R.drawable.large_logo),
            contentDescription = stringResource(R.string.app_name),
            modifier = Modifier.size(200.dp)
        )

        CircularProgressIndicator(
            modifier = Modifier.width(64.dp),
            color = colorScheme.tertiary
        )
    }
}

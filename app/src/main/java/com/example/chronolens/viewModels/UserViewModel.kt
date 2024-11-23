package com.example.chronolens.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chronolens.repositories.UserRepository
import com.example.chronolens.utils.EventBus
import com.example.chronolens.utils.Prefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.HttpURLConnection

enum class UserLoginState {
    LoggedIn,
    LoggedOut,
    CredentialsWrong,
    Error,
    Loading
}

data class UserState(
    val username: String = "",
    val server: String = "",
    val userLoginState: UserLoginState = UserLoginState.LoggedOut
)

class UserViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _userState = MutableStateFlow(UserState())
    val userState: StateFlow<UserState> = _userState.asStateFlow()

    init {
        viewModelScope.launch {
            val loggedIn = userRepository.checkLogin()
            val state: UserLoginState
            if (loggedIn) {
                state = UserLoginState.LoggedIn
                _userState.update { currState ->
                    currState.copy(
                        userLoginState = state,
                        server = getServer(),
                        username = getUsername()
                    )
                }
            } else {
                logout()
            }
        }
    }


    fun getServer(): String {
        return userRepository.sharedPreferences.getString(Prefs.SERVER, "")!!
    }

    fun getUsername(): String {
        return userRepository.sharedPreferences.getString(Prefs.USERNAME, "")!!
    }

    fun login(server: String, username: String, password: String) {
        Log.i("LOGIN", "$server | $username | $password")
        viewModelScope.launch {
            _userState.update { currState ->
                currState.copy(userLoginState = UserLoginState.Loading)
            }
            val code: Int? = userRepository.apiLogin(server, username, password)
            Log.i("LOGIN", "$code")
            when (code) {
                HttpURLConnection.HTTP_OK -> _userState.update { currState ->
                    currState.copy(
                        userLoginState = UserLoginState.LoggedIn,
                        username = username,
                        server = server
                    )
                }

                HttpURLConnection.HTTP_UNAUTHORIZED -> _userState.update { currState ->
                    currState.copy(
                        userLoginState = UserLoginState.CredentialsWrong,
                    )
                }

                else -> _userState.update { currState ->
                    currState.copy(
                        userLoginState = UserLoginState.Error,
                    )
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            userRepository.logout()
            _userState.update { currState ->
                currState.copy(userLoginState = UserLoginState.LoggedOut)
            }
        }
    }

}
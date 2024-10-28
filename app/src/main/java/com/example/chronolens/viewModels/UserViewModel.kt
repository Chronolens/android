package com.example.chronolens.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chronolens.repositories.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class UserLoginState {
    LoggedIn,
    LoggedOut,
    CredentialsWrong,
    Error,
    Loading
}

data class UserState(
    val username: String = "",
    val userLoginState: UserLoginState = UserLoginState.LoggedOut
    // TODO: the rest of the user's stuff
)

class UserViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _userState = MutableStateFlow(UserState())
    val userState: StateFlow<UserState> = _userState.asStateFlow()


    init {
        // TODO: Check for token, if yes, grantAccess
        //userRepository.checkLogin()

    }

    fun getServer():String{
        return userRepository.sharedPreferences.getString("SERVER","")!!
    }

    // TODO: Check for code and change userLoginState
    fun login(server: String, username: String, password: String) {
        viewModelScope.launch {
            _userState.update { currState ->
                currState.copy(userLoginState = UserLoginState.Loading)
            }
            val code: Int? = userRepository.apiLogin(server,username, password)
            when (code) {
                200 -> _userState.update { currState ->
                    currState.copy(
                        userLoginState = UserLoginState.LoggedIn,
                        username = username
                    )
                }

                401 -> _userState.update { currState ->
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

}
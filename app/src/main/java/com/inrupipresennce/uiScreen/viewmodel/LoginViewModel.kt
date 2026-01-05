package com.inrupipresennce.uiScreen.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inrupipresennce.data.model.LoginResult
import com.inrupipresennce.data.repositry.LoginRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: LoginRepository) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginResult?>(null)
    val loginState: StateFlow<LoginResult?> = _loginState

    fun login(phone: String) {
        if (phone.isBlank() || phone.length != 10) {
            _loginState.value = LoginResult(
                success = false,
                message = "Please enter a valid 10-digit phone number"
            )
            return
        }

        viewModelScope.launch {

            // VERY IMPORTANT: reset previous value so LaunchedEffect WILL run next time
            _loginState.value = null

            try {
                val result = repository.login(phone)

                _loginState.value = result   // success OR fail from server

            } catch (e: Exception) {

                _loginState.value = LoginResult(
                    success = false,
                    message = e.message ?: "Unable to connect, please try again."
                )
            }
        }
    }



    fun checkLogin(): Pair<Boolean, Int?> {
        return repository.checkLogin()
    }
}


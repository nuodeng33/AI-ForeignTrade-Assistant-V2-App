package com.example.maolianzhihe.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.maolianzhihe.data.repository.AuthRepository
import com.example.maolianzhihe.model.AuthResponse
import com.example.maolianzhihe.ui.UiState
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {
    private val _loginState = MutableLiveData<UiState<AuthResponse>>(UiState.Idle)
    val loginState: LiveData<UiState<AuthResponse>> = _loginState

    fun login(identifier: String, password: String) {
        _loginState.value = UiState.Loading
        viewModelScope.launch {
            _loginState.value = repository.login(identifier, password)
        }
    }
}

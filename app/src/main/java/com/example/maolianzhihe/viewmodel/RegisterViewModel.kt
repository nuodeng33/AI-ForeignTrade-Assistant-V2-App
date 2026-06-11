package com.example.maolianzhihe.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.maolianzhihe.data.repository.AuthRepository
import com.example.maolianzhihe.model.AuthResponse
import com.example.maolianzhihe.ui.UiState
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {
    private val _registerState = MutableLiveData<UiState<AuthResponse>>(UiState.Idle)
    val registerState: LiveData<UiState<AuthResponse>> = _registerState

    fun register(username: String, email: String, password: String) {
        _registerState.value = UiState.Loading
        viewModelScope.launch {
            _registerState.value = repository.register(username, email, password)
        }
    }
}

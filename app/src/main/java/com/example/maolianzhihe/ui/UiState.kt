package com.example.maolianzhihe.ui

sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Empty(val message: String) : UiState<Nothing>()
    data class Error(val message: String, val code: Int? = null) : UiState<Nothing>()
}

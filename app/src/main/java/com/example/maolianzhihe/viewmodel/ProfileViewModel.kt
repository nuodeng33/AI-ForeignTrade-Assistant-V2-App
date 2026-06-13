package com.example.maolianzhihe.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.maolianzhihe.data.repository.ProfileRepository
import com.example.maolianzhihe.model.ProfileSummary
import com.example.maolianzhihe.ui.UiState
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: ProfileRepository = ProfileRepository()
) : ViewModel() {
    private val _profileSummaryState = MutableLiveData<UiState<ProfileSummary>>(UiState.Idle)
    val profileSummaryState: LiveData<UiState<ProfileSummary>> = _profileSummaryState

    fun loadProfileSummary() {
        _profileSummaryState.value = UiState.Loading
        viewModelScope.launch {
            _profileSummaryState.value = repository.getProfileSummary()
        }
    }
}

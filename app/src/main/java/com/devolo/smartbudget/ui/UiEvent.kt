package com.devolo.smartbudget.ui

sealed class UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent()
    object ShowOnboarding : UiEvent()
}

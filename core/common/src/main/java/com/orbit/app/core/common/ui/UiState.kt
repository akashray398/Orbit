package com.orbit.app.core.common.ui

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<out T>(val data: T) : UiState<T>
    data class Empty(val message: String = "No content available") : UiState<Nothing>
    data class Error(val message: String, val onRetry: (() -> Unit)? = null) : UiState<Nothing>
}

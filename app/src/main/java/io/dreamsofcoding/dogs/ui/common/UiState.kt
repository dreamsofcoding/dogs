package io.dreamsofcoding.dogs.ui.common

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(
        val error: UiError,
        val throwable: Throwable? = null
    ) : UiState<Nothing>()
}
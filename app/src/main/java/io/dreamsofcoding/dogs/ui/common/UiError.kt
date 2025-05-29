package io.dreamsofcoding.dogs.ui.common

import io.dreamsofcoding.dogs.remote.DogsException

sealed class UiError {
    object NoNetwork : UiError()
    object InvalidBreed : UiError()
    object ServerError : UiError()
    object Unknown : UiError()
}
    fun mapToUiError(e: Throwable): UiError =
        when (e) {
            is DogsException.NetworkException -> UiError.NoNetwork
            is DogsException.InvalidBreedException -> UiError.InvalidBreed
            is DogsException.ApiException -> UiError.ServerError
            else -> UiError.Unknown
        }
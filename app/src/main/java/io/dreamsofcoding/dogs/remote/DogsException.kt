package io.dreamsofcoding.dogs.remote

sealed class DogsException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {

    class NetworkException(
        message: String = "Network error occurred",
        cause: Throwable? = null
    ) : DogsException(message, cause)

    class ApiException(
        val code: Int,
        message: String = "API error occurred",
        cause: Throwable? = null
    ) : DogsException("API Error ($code): $message", cause)

    class InvalidBreedException(
        breed: String,
        cause: Throwable? = null
    ) : DogsException("Invalid breed: '$breed'", cause)

    class UnknownException(
        message: String = "Unknown error occurred",
        cause: Throwable? = null
    ) : DogsException(message, cause)
}
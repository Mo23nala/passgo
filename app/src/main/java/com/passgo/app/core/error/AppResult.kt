package com.passgo.app.core.error

sealed class AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>()
    data class Error(val exception: AppException) : AppResult<Nothing>()
}

sealed class AppException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class NetworkException(message: String, cause: Throwable? = null) : AppException(message, cause)
    class DatabaseException(message: String, cause: Throwable? = null) : AppException(message, cause)
    class AuthenticationException(message: String, cause: Throwable? = null) : AppException(message, cause)
    class UnknownException(message: String, cause: Throwable? = null) : AppException(message, cause)

    companion object {
        fun fromThrowable(throwable: Throwable): AppException {
            return when (throwable) {
                is AppException -> throwable
                else -> UnknownException(throwable.message ?: "Unknown error", throwable)
            }
        }
    }
}

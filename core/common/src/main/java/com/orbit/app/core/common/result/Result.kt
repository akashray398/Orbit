package com.orbit.app.core.common.result

import com.orbit.app.core.domain.model.AppError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed interface Result<out T> {
    data class Success<out T>(val data: T) : Result<T>
    data class Error(
        val exception: Throwable,
        val appError: AppError = (exception as? AppError) ?: AppError.Unknown(exception.message, exception)
    ) : Result<Nothing>
    data object Loading : Result<Nothing>
}

inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> {
    return when (this) {
        is Result.Success -> Result.Success(transform(data))
        is Result.Error -> Result.Error(exception, appError)
        is Result.Loading -> Result.Loading
    }
}

inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) {
        action(data)
    }
    return this
}

inline fun <T> Result<T>.onError(action: (Throwable, AppError) -> Unit): Result<T> {
    if (this is Result.Error) {
        action(exception, appError)
    }
    return this
}

suspend fun <T> safeCall(
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    block: suspend () -> T
): Result<T> {
    return withContext(dispatcher) {
        try {
            Result.Success(block())
        } catch (appError: AppError) {
            Result.Error(appError, appError)
        } catch (e: Throwable) {
            Result.Error(e, AppError.Unknown(e.message, e))
        }
    }
}

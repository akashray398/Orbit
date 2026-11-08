package com.orbit.app.core.common.result

import com.orbit.app.core.domain.model.AppError
import com.orbit.app.core.domain.result.Result
import com.orbit.app.core.domain.result.map as domainMap
import com.orbit.app.core.domain.result.onError as domainOnError
import com.orbit.app.core.domain.result.onSuccess as domainOnSuccess
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

typealias Result<T> = com.orbit.app.core.domain.result.Result<T>

inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> = this.domainMap(transform)

inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> = this.domainOnSuccess(action)

inline fun <T> Result<T>.onError(action: (Throwable, AppError) -> Unit): Result<T> = this.domainOnError(action)

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

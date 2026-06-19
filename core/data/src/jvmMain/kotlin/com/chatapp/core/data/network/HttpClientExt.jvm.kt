package com.chatapp.core.data.network

import com.chatapp.core.domain.util.DataError
import com.chatapp.core.domain.util.Result
import io.ktor.client.statement.HttpResponse

actual suspend fun <T> platformSafeCall(
    execute: suspend () -> HttpResponse,
    handleResponse: suspend (HttpResponse) -> Result<T, DataError.Remote>
): Result<T, DataError.Remote> {
    // TODO: Implement this later.
    return Result.Failure(DataError.Remote.UNKNOWN)
}
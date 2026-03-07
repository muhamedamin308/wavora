package com.wavora.app.core.result

/**
 * @author Muhamed Amin Hassan on 07,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */
sealed interface AsyncResult<out T> {
    data object Loading : AsyncResult<Nothing>
    data class Success<T>(val data: T) : AsyncResult<T>
    data class Error(val message: String, val cause: Throwable? = null) : AsyncResult<Nothing>
}

val <T> AsyncResult<T>.isLoading: Boolean
    get() = this is AsyncResult.Loading

val <T> AsyncResult<T>.isError: Boolean
    get() = this is AsyncResult.Error

fun <T> AsyncResult<T>.dataOrNull(): T? = (this as? AsyncResult.Success)?.data
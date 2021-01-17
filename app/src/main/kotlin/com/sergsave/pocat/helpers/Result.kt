package com.sergsave.pocat.helpers

sealed class Result<T> {
    data class Success<T>(val value: T): Result<T>()
    data class Error<T>(val t: Throwable): Result<T>()
}
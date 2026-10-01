package com.mmmut.ero.core

sealed interface RepoResult<out T> {
    data class Ok<T>(val value: T) : RepoResult<T>
    data class Err(val message: String) : RepoResult<Nothing>
}

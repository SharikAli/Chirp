package com.chatapp.core.domain.util

class DataErrorException(
    val error: DataError
): Exception()
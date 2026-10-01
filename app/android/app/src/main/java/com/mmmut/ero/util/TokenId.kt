package com.mmmut.ero.util

object TokenId {
    fun docId(token: String): String {
        var h = 0; for (c in token) h = ((h shl 5) - h + c.code)
        return "android_" + kotlin.math.abs(h).toString(36)
    }
}

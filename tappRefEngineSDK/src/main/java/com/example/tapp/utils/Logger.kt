package com.example.tapp.utils

object Logger {
    private const val ENABLE_LOGGING = true // Toggle for enabling/disabling logging

    fun logError(error: String) {
        if (ENABLE_LOGGING) {
            println("Tapp-Error: $error")
        }
    }

    fun logInfo(message: String) {
        if (ENABLE_LOGGING) {
            println("Tapp-Info: $message")
        }
    }

    fun logDebug(message: String) {
        if (ENABLE_LOGGING) {
            println("Tapp-Debug: $message")
        }
    }

    fun logWarning(message: String) {
        if (ENABLE_LOGGING) {
            println("Tapp-Warning: $message")
        }
    }
}

package com.example.tapp.models

import kotlinx.serialization.Serializable
import java.net.URL

@Serializable
enum class Affiliate {
    ADJUST,
    APPFLYER,
    TAPP;

    fun toIntValue(): Int {
        return when (this) {
            ADJUST -> 1
            APPFLYER -> 2
            TAPP -> 3
        }
    }
}

// URL parameter keys for each affiliate
enum class AdjustURLParamKey(val value: String) {
    TOKEN("adj_t")
}

enum class AppsflyerURLParamKey(val value: String) {
    TOKEN("af_t")
}

enum class TappURLParamKey(val value: String) {
    TOKEN("tapp_t")
}

// Extension function to extract a query parameter from a URL.
fun URL.param(key: String): String? {
    return this.query
        ?.split("&")
        ?.map { it.split("=") }
        ?.firstOrNull { it[0] == key }
        ?.getOrNull(1)
}
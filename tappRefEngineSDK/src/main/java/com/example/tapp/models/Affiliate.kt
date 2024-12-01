package com.example.tapp.models

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

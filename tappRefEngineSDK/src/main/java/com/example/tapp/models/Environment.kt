package com.example.tapp.models

import kotlinx.serialization.Serializable

//@Serializable
enum class Environment {
    production,
    sandbox;

    fun environmentName(): String {
        return name
    }
}

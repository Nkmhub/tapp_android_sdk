package com.example.tapprefenginesdk.config

import android.content.Context
import com.example.tapprefenginesdk.models.Environment
import com.example.tapprefenginesdk.R

class ConfigManager(private val context: Context) {

    fun getWreToken(): String {
        return try {
            context.getString(context.resources.getIdentifier("WRE_TOKEN", "string", context.packageName))
        } catch (e: Exception) {
            throw IllegalArgumentException("WRE_TOKEN not found in app's resources. Please define it in tapp_config.xml.")
        }
    }

    fun getEnvironment(): Environment {
        val environmentStr = context.getString(context.resources.getIdentifier("ENVIRONMENT", "string", context.packageName))
        return when (environmentStr.lowercase()) {
            "production" -> Environment.production
            "sandbox" -> Environment.sandbox
            else -> throw IllegalArgumentException("Invalid environment value in tapp_config.xml: $environmentStr")
        }
    }

    fun getAppToken(): String {
        return try {
            context.getString(context.resources.getIdentifier("APP_TOKEN", "string", context.packageName))
        } catch (e: Exception) {
            throw IllegalArgumentException("APP_TOKEN not found in app's resources. Please define it in tapp_config.xml.")
        }
    }

    fun getTappToken(): String {
        return try {
            context.getString(context.resources.getIdentifier("TAPP_TOKEN", "string", context.packageName))
        } catch (e: Exception) {
            throw IllegalArgumentException("TAPP_TOKEN not found in app's resources. Please define it in tapp_config.xml.")
        }
    }
}

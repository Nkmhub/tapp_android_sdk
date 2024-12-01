package com.example.tapp.services.network

import com.example.tapp.dependencies.Dependencies
import com.example.tapp.models.Environment

object TappEndpoint {
    private fun getBaseUrl(env: String): String {
        val environment = try {
            Environment.valueOf(env.lowercase().replaceFirstChar { it.uppercaseChar() }) // Maps "sandbox" or "production" to the enum
        } catch (e: IllegalArgumentException) {
            throw TappError.MissingConfiguration("Invalid environment value: $env")
        }

        return when (environment) {
            Environment.production -> "https://api.nkmhub.com/v1/ref/"
            Environment.sandbox -> "https://api.nkmhub.com/v2/ref/"
        }
    }

    // Endpoint for handling deeplink impressions
    fun deeplink(dependencies: Dependencies, deepLink: String): RequestModels.Endpoint {
        val config = dependencies.keystoreUtils.getConfig()
            ?: throw TappError.MissingConfiguration("Missing configuration in keystore")

        val url = "${getBaseUrl(config.env)}deeplink"
        val headers = mapOf(
            "Content-Type" to "application/json",
            "Authorization" to "Bearer ${config.authToken}"
        )
        val body = mapOf(
            "tapp_token" to config.tappToken,
            "bundleID" to (config.bundleID?:""),
            "deepLink" to deepLink
        )

        return RequestModels.Endpoint(url, headers, body)
    }

    // Endpoint for fetching secrets
    fun secrets(dependencies: Dependencies): RequestModels.Endpoint {
        val config = dependencies.keystoreUtils.getConfig()
            ?: throw TappError.MissingConfiguration("Missing configuration in keystore")

        val url = "${getBaseUrl(config.env)}secrets"
        val headers = mapOf(
            "Content-Type" to "application/json",
            "Authorization" to "Bearer ${config.authToken}"
        )
        val body = mapOf(
            "tapp_token" to config.tappToken,
            "bundleID" to (config.bundleID?:""),
            "mmp" to config.affiliate.toIntValue()
        )

        return RequestModels.Endpoint(url, headers, body)
    }


    fun generateAffiliateUrl(dependencies: Dependencies,request: RequestModels.GenerateAffiliateUrlRequest):  RequestModels.Endpoint  {
        val config = dependencies.keystoreUtils.getConfig()
            ?: throw TappError.MissingConfiguration("Configuration is missing")

        val url = "${getBaseUrl(config.env)}influencer/add"

        val headers = mapOf(
            "Authorization" to "Bearer ${request.authToken}",
            "Content-Type" to "application/json"
        )

        val body = mapOf(
            "tapp_token" to request.tappToken,
            "bundle_id" to (config.bundleID?:""),
            "mmp" to request.mmp,
            "adgroup" to (request.adGroup ?: ""),
            "creative" to (request.creative ?: ""),
            "influencer" to request.influencer,
            "data" to (request.data ?: emptyMap())
        )

        return RequestModels.Endpoint(url, headers, body)
    }

    fun tappEvent(
        dependencies: Dependencies,
        eventRequest: RequestModels.TappEventRequest
    ): RequestModels.Endpoint {
        val config = dependencies.keystoreUtils.getConfig()
            ?: throw TappError.MissingConfiguration("Configuration is missing")

        val url = "${getBaseUrl(config.env)}/event"
        val headers = mapOf(
            "Content-Type" to "application/json",
            "Authorization" to "Bearer ${config.authToken}"
        )

        val body = mapOf(
            "tapp_token" to config.tappToken,
            "bundle_id" to (config.bundleID?:""),
            "event_name" to eventRequest.eventName,
            "event_action" to eventRequest.eventAction,
            "event_custom_action" to eventRequest.eventCustomAction
        ).filterValues { it != null } // Remove null entries

        return RequestModels.Endpoint(url, headers, body)
    }


}

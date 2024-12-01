package com.example.tapp.services.network

import com.example.tapp.utils.TappConfiguration

class RequestModels {

    data class ImpressionRequest(
        val tappToken: String,
        val bundleID: String,
        val deepLink: String
    )

    data class SecretsRequest(
        val tappToken: String,
        val bundleID: String,
        val mmp: Int
    )

    data class SecretsResponse(val secret: String)

    data class Endpoint(
        val url: String,
        val headers: Map<String, String>,
        val body: Map<String, Any>
    )

    data class GenerateAffiliateUrlRequest(
        val tappToken: String,
        val mmp: Int,
        val influencer: String,
        val adGroup: String?,
        val creative: String?,
        val data: Map<String, Any>?,
        val authToken: String
    )

    data class AffiliateUrlRequest(
        val influencer: String,
        val adGroup: String?,
        val creative: String?,
        val data: Map<String, String>? = null
    ) {
        fun toRequestBody(config: TappConfiguration): Map<String, Any> {
            return mapOf(
                "wre_token" to config.tappToken,
                "mmp" to config.affiliate.toIntValue(),
                "influencer" to influencer,
                "adgroup" to (adGroup ?: ""),
                "creative" to (creative ?: ""),
                "data" to (data ?: emptyMap())
            )
        }
    }
    data class AffiliateUrlResponse(
        val error: Boolean,
        val message: String,
        val influencer_url: String
    )

    data class TappEventRequest(
        val eventName: String,        // Name of the event
        val eventAction: Int,         // 1: Click, 2: Impression, 3: Count, -1: Custom
        val eventCustomAction: Any    // false or a String if eventAction is -1
    )
    data class TappEvent(
        val eventName: String,
        val eventAction: EventAction
    )

    data class TappEventResponse(
        val error: Boolean,
        val message: String,
    )

    sealed class EventAction(val rawValue: Int) {
        data object Click : EventAction(1)
        data object Impression : EventAction(2)
        data object Count : EventAction(3)
        data class Custom(val customValue: String) : EventAction(-1)

        // Determine if the action is custom
        val isCustom: Boolean
            get() = this is Custom

        // Check if the action is valid
        val isValid: Boolean
            get() = when (this) {
                is Custom -> customValue.isNotEmpty()
                else -> true
            }

        // Return the custom action string or default "false" for non-custom actions
        val eventCustomAction: String
            get() = when (this) {
                is Custom -> if (customValue.isNotEmpty()) customValue else "false"
                else -> "false"
            }
    }

}
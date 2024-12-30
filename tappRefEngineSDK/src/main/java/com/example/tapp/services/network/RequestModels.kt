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

    data class TappUrlResponse(
        val error: Boolean,
        val message: String,
    )

    sealed class EventAction(val rawValue: Int) {
//        data object Click : EventAction(1)
//        data object Impression : EventAction(2)
//        data object Count : EventAction(3)
        data object TAPP_ADD_PAYMENT_INFO : EventAction(1)
        data object TAPP_ADD_TO_CART : EventAction(2)
        data object TAPP_ADD_TO_WISHLIST : EventAction(3)
        data object TAPP_COMPLETE_REGISTRATION : EventAction(4)
        data object TAPP_CONTACT : EventAction(5)
        data object TAPP_CUSTOMIZE_PRODUCT : EventAction(6)
        data object TAPP_DONATE : EventAction(7)
        data object TAPP_FIND_LOCATION : EventAction(8)
        data object TAPP_INITIATE_CHECKOUT : EventAction(9)
        data object TAPP_GENERATE_LEAD : EventAction(10)
        data object TAPP_PURCHASE : EventAction(11)
        data object TAPP_SCHEDULE : EventAction(12)
        data object TAPP_SEARCH : EventAction(13)
        data object TAPP_START_TRIAL : EventAction(14)
        data object TAPP_SUBMIT_APPLICATION : EventAction(15)
        data object TAPP_SUBSCRIBE : EventAction(16)
        data object TAPP_VIEW_CONTENT : EventAction(17)
        data object TAPP_CLICK_BUTTON : EventAction(18)
        data object TAPP_DOWNLOAD_FILE : EventAction(19)
        data object TAPP_JOIN_GROUP : EventAction(20)
        data object TAPP_ACHIEVE_LEVEL : EventAction(21)
        data object TAPP_CREATE_GROUP : EventAction(22)
        data object TAPP_CREATE_ROLE : EventAction(23)
        data object TAPP_LINK_CLICK : EventAction(24)
        data object TAPP_LINK_IMPRESSION : EventAction(25)
        data object TAPP_APPLY_FOR_LOAN : EventAction(26)
        data object TAPP_LOAN_APPROVAL : EventAction(27)
        data object TAPP_LOAN_DISBURSAL : EventAction(28)
        data object TAPP_LOGIN : EventAction(29)
        data object TAPP_RATE : EventAction(30)
        data object TAPP_SPEND_CREDITS : EventAction(31)
        data object TAPP_UNLOCK_ACHIEVEMENT : EventAction(32)
        data object TAPP_ADD_SHIPPING_INFO : EventAction(33)
        data object TAPP_EARN_VIRTUAL_CURRENCY : EventAction(34)
        data object TAPP_START_LEVEL : EventAction(35)
        data object TAPP_COMPLETE_LEVEL : EventAction(36)
        data object TAPP_POST_SCORE : EventAction(37)
        data object TAPP_SELECT_CONTENT : EventAction(38)
        data object TAPP_BEGIN_TUTORIAL : EventAction(39)
        data object TAPP_COMPLETE_TUTORIAL : EventAction(40)

        data class Custom(val customValue: String) : EventAction(0)

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
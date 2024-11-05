package com.example.tapprefenginesdk.services.affiliate.adjust

import android.content.Context
import android.net.Uri
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustConfig
import com.adjust.sdk.AdjustDeeplink
import com.adjust.sdk.AdjustEvent
import com.adjust.sdk.LogLevel
import com.example.tapprefenginesdk.models.Affiliate
import com.example.tapprefenginesdk.models.AffiliateUrlResponse
import com.example.tapprefenginesdk.models.Environment
import com.example.tapprefenginesdk.services.affiliate.AffiliateService

class AdjustAffiliateService : AffiliateService {

    override fun processReferral(
        context: Context,
        deepLink: String,
        environment: Environment,
        appToken: String
    ): Boolean {
        return try {
            println("Handling Adjust referral")
            println("Environment: $environment")
            println("App Token: $appToken")
            println("Deep Link: $deepLink")

            val adjustEnvironment = when (environment) {
                Environment.production -> AdjustConfig.ENVIRONMENT_PRODUCTION
                Environment.sandbox -> AdjustConfig.ENVIRONMENT_SANDBOX
            }

            val config = AdjustConfig(context, appToken, adjustEnvironment)
            config.setLogLevel(LogLevel.VERBOSE)
            Adjust.initSdk(config)

            // Call handleCallback after initializing the Adjust SDK if needed
            // handleCallback(context, deepLink)

            // Return true to indicate successful processing
            true
        } catch (e: Exception) {
            // Log the error
            println("Error during Adjust referral processing: ${e.message}")

            // Return false to indicate a failure
            false
        }
    }


    override fun handleCallback(context: Context,deepLink: String) {
        val incomingUri = Uri.parse(deepLink)
        val url = AdjustDeeplink(incomingUri)
        Adjust.processDeeplink(url, context)
        println("Adjust notified of the incoming URL: $incomingUri")
    }

    override suspend fun handleEvent(eventId: String, authToken: String?) {
        val adjustEvent = AdjustEvent(eventId)
        Adjust.trackEvent(adjustEvent)
        println("Adjust tracked Event for event_id: $eventId")
    }

    override suspend fun generateAffiliateUrl(
        wreToken: String,
        influencer: String,
        adgroup: String,
        creative: String,
        mmp: Affiliate,
        token: String,
        jsonObject: Map<String, Any>
    ): AffiliateUrlResponse {
        TODO("Adjust: Not yet implemented")
    }

}

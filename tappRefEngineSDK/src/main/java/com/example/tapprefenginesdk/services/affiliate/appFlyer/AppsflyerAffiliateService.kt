package com.example.tapprefenginesdk.services.affiliate.appFlyer

import android.content.Context
import com.example.tapprefenginesdk.models.Affiliate
import com.example.tapprefenginesdk.models.AffiliateUrlResponse
import com.example.tapprefenginesdk.models.Environment
import com.example.tapprefenginesdk.services.affiliate.AffiliateService

class AppsflyerAffiliateService : AffiliateService {
    override fun processReferral(
        context: Context,
        deepLink: String,
        environment: Environment,
        appToken: String
    ): Boolean {
        println("Handling AppFlyer referral")
        println("App Token: $appToken")
        println("Deep Link: $deepLink")

        TODO("APPFLYER: Add your AppFlyer-specific logic here")
        return false;

    }

    override fun handleCallback(context: Context, deepLink: String) {
        TODO("APPFLYER: Not yet implemented")
    }

    override suspend fun handleEvent(eventId: String, authToken: String?) {
        TODO("APPFLYER: Not yet implemented")
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
        TODO("APPFLYER: Not yet implemented")
    }
}

package com.example.tapprefenginesdk

import android.content.Context
import com.example.tapprefenginesdk.models.Affiliate
import com.example.tapprefenginesdk.models.AdjustEnv
import com.example.tapprefenginesdk.models.AffiliateUrlResponse
import com.example.tapprefenginesdk.services.affiliate.AffiliateServiceFactory
import com.example.tapprefenginesdk.services.network.NetworkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RefEngine{

    private val networkManager = NetworkManager()

    fun processReferralEngine(
        context: Context,
        deepLink: String,
        environment: AdjustEnv,
        affiliate: Affiliate,
        appToken: String
    ) {
        val sharedPreferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val isFirstTime = sharedPreferences.getBoolean("isFirstTimeReferral", true)

        if (isFirstTime) {
            println("Processing deep link for the first time: $deepLink")

            val affiliateService = AffiliateServiceFactory.getAffiliateService(affiliate)
            affiliateService?.processReferral(context, deepLink, environment, appToken)

            // Mark referral as processed
            sharedPreferences.edit().putBoolean("isFirstTimeReferral", false).apply()
        } else {
            println("Referral engine has already been processed before.")
        }
    }

    suspend fun affiliateUrl(
        wreToken: String,
        influencer: String,
        adgroup: String,
        creative: String,
        mmp: Affiliate,
        token: String,
        jsonObject: Map<String, Any>
    ): AffiliateUrlResponse = withContext(Dispatchers.IO) {
        try {
            val response = networkManager.generateAffiliateUrl(
                wreToken,
                influencer,
                adgroup,
                creative,
                mmp,
                token,
                jsonObject
            )

            AffiliateUrlResponse(
                error = response.error,
                message = response.message,
                influencer_url = response.influencer_url
            )
        } catch (e: Exception) {
            AffiliateUrlResponse(
                error = true,
                message = "Something went wrong",
                influencer_url = ""
            )
        }
    }
}

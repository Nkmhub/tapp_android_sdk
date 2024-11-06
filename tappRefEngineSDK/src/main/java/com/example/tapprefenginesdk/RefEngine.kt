package com.example.tapprefenginesdk

import android.content.Context
import com.example.tapprefenginesdk.config.ConfigManager
import com.example.tapprefenginesdk.models.Affiliate
import com.example.tapprefenginesdk.models.AffiliateUrlResponse
import com.example.tapprefenginesdk.models.Environment
import com.example.tapprefenginesdk.services.affiliate.AffiliateServiceFactory
import com.example.tapprefenginesdk.services.network.NetworkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RefEngine(context: Context) {

    private val networkManager = NetworkManager()
    private val configManager: ConfigManager
    private val wreToken: String
    private val environment: Environment
    private val appToken: String
    private val tappToken: String

    init {
        // Initialize ConfigManager and load configuration values
        configManager = ConfigManager(context)
        wreToken = configManager.getWreToken()
        environment = configManager.getEnvironment()
        appToken = configManager.getAppToken()
        tappToken = configManager.getTappToken()
    }

    suspend fun processReferralEngine(
        context: Context,
        deepLink: String,
        affiliate: Affiliate,
    ) {
        println("environment: $environment")
        println("appToken: $appToken")
        println("tappToken: $tappToken")

        val sharedPreferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val isFirstTime = sharedPreferences.getBoolean("isFirstTimeReferral", true)

        val affiliateService = AffiliateServiceFactory.getAffiliateService(affiliate)
        val initialize = affiliateService?.processReferral(context, deepLink, environment, appToken)

        if (isFirstTime && initialize == true) {
            println("Processing deep link for the first time: $deepLink")
            affiliateService?.handleCallback(context, deepLink)

            // Mark referral as processed
            sharedPreferences.edit().putBoolean("isFirstTimeReferral", false).apply()
        } else if (!isFirstTime) {
            println("Referral engine has already been processed before.")
        } else {
            println("Initialization is not completed.")
        }
    }

    suspend fun affiliateUrl(
        influencer: String,
        adgroup: String,
        creative: String,
        mmp: Affiliate,
        jsonObject: Map<String, Any>
    ): AffiliateUrlResponse = withContext(Dispatchers.IO) {
        // Create an instance of TappAffiliateService
        val tappAffiliateService = AffiliateServiceFactory.getAffiliateService(Affiliate.TAPP)

        // Call the generateAffiliateUrl method and return the result
        tappAffiliateService?.generateAffiliateUrl(
            wreToken = wreToken,
            influencer = influencer,
            adgroup = adgroup,
            creative = creative,
            mmp = mmp,
            token = tappToken,
            jsonObject = jsonObject
        ) ?: AffiliateUrlResponse(
            error = true,
            message = "Affiliate service not available",
            influencer_url = ""
        )
    }


    suspend fun eventHandler(affiliate: Affiliate, eventToken: String) {
        // Use factory to create the right affiliate service
        val affiliateService = AffiliateServiceFactory.getAffiliateService(affiliate)
        affiliateService?.handleEvent(eventToken, tappToken)
    }
}

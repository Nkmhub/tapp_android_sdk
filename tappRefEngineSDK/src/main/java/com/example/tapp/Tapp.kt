package com.example.tapp

import android.content.Context
import android.net.Uri
import com.example.tapp.dependencies.Dependencies
import com.example.tapp.models.Affiliate
import com.example.tapp.services.affiliate.tapp.TappAffiliateService
import com.example.tapp.services.network.RequestModels
import com.example.tapp.services.network.TappError
import com.example.tapp.utils.Logger
import com.example.tapp.utils.TappConfiguration
import com.example.tapp.utils.VoidCompletion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.provider.Settings

class Tapp(context: Context) {

    internal val dependencies = Dependencies.live(context)
    fun start(config: TappConfiguration) {
        Logger.logInfo("Start config")
        val androidId = Settings.Secure.getString(
            dependencies.context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
        Logger.logInfo("Android_id saved, $androidId")

        Logger.logInfo("Get bundle id")
        val updatedConfig = config.copy(
            bundleID = config.bundleID ?: dependencies.context.packageName,
            androidId = config.androidId ?: androidId
        )
        Logger.logInfo("updatedConfig $updatedConfig")
        val storedConfig = dependencies.keystoreUtils.getConfig()
        if (storedConfig == null || storedConfig != updatedConfig) {
            Logger.logInfo("Trying to save the config")
            dependencies.keystoreUtils.saveConfig(updatedConfig)
            Logger.logInfo("Configuration saved: $updatedConfig")
        } else {
            Logger.logInfo("Configuration already exists: $storedConfig")
        }

        CoroutineScope(Dispatchers.Main).launch {
            fetchSecretsAndInitializeReferralEngineIfNeeded { result ->
                result.fold(
                    onSuccess = {
                        Logger.logInfo("Successfully initialized referral engine.")
                    },
                    onFailure = { error ->
                        Logger.logWarning("Failed to initialize referral engine: ${error.localizedMessage}")
                    }
                )
            }
        }
    }



    fun appWillOpen(url: String?, completion: VoidCompletion?) {

        if (url.isNullOrEmpty()) {
            Logger.logInfo("No URL provided. Skipping appWillOpen processing.")
            completion?.invoke(Result.success(Unit))
            return
        }

        // Step 1: Check if the referral engine is already processed
        if (hasProcessedReferralEngine()) {
            Logger.logInfo("Referral engine already processed. Returning early.")
            completion?.invoke(Result.success(Unit)) // Optionally signal success without processing
            return
        }

        val parsedUri = Uri.parse(url)
        // Step 2: Get the configuration
        val config = dependencies.keystoreUtils.getConfig()
        if (config == null) {
            completion?.invoke(Result.failure(TappError.MissingConfiguration()))
            return
        }

        // Step 3: Continue to the next logic
        appWillOpen(parsedUri, completion)
    }

    fun logConfig() {
        // Retrieve the stored configuration
        val storedConfig = dependencies.keystoreUtils.getConfig()

        if (storedConfig == null) {
            Logger.logError("No configuration found.")
            return
        }

        // Log all the details of the configuration
        Logger.logInfo("Current SDK Configuration:")
        Logger.logInfo("Auth Token: ${storedConfig.authToken}")
        Logger.logInfo("Environment: ${storedConfig.env}")
        Logger.logInfo("Tapp Token: ${storedConfig.tappToken}")
        Logger.logInfo("Affiliate: ${storedConfig.affiliate}")
        Logger.logInfo("Bundle ID: ${storedConfig.bundleID ?: "Not Set"}")
        Logger.logInfo("Android ID: ${storedConfig.androidId ?: "Not Set"}")
        Logger.logInfo("App Token: ${storedConfig.appToken ?: "Not Set"}")
        Logger.logInfo("Referral Engine Processed: ${storedConfig.hasProcessedReferralEngine}")
    }


    suspend fun url(
        influencer: String,
        adGroup: String?,
        creative: String?,
        data: Map<String, String>? = null
    ): RequestModels.AffiliateUrlResponse = withContext(Dispatchers.IO) {
        dependencies.keystoreUtils.getConfig()
            ?: return@withContext RequestModels.AffiliateUrlResponse(
                error = true,
                message = "Missing configuration",
                influencer_url = ""
            )

        val request = RequestModels.AffiliateUrlRequest(
            influencer = influencer,
            adGroup = adGroup,
            creative = creative,
            data = data
        )

        val tappService =
            dependencies.affiliateServiceFactory.getAffiliateService(Affiliate.TAPP, dependencies)
        if (tappService !is TappAffiliateService) {
            return@withContext RequestModels.AffiliateUrlResponse(
                error = true,
                message = "Tapp service not available",
                influencer_url = ""
            )
        }

        tappService.generateAffiliateUrl(request)
    }

    fun handleEvent(eventToken: String) {
        // Step 1: Fetch the configuration from dependencies
        val config = dependencies.keystoreUtils.getConfig()
            ?: throw TappError.MissingConfiguration("Missing configuration")

        // Step 2: Get the affiliate from the configuration
        val affiliate = config.affiliate

        // Step 3: Get the appropriate affiliate service
        val affiliateService =
            dependencies.affiliateServiceFactory.getAffiliateService(affiliate, dependencies)
                ?: throw TappError.MissingAffiliateService("Affiliate service not available for $affiliate")

        // Step 4: Call the handleEvent method
        affiliateService.handleEvent(eventToken)
    }

    fun handleTappEvent(tappEvent: RequestModels.TappEvent) {
        CoroutineScope(Dispatchers.IO).launch {
            val tappService = dependencies.affiliateServiceFactory.getAffiliateService(
                Affiliate.TAPP,
                dependencies
            )
            if (tappService !is TappAffiliateService) {
                Logger.logError("Tapp service not available")
                return@launch
            }

            val tappEventRequest = RequestModels.TappEventRequest(
                eventName = tappEvent.eventName,
                eventAction = tappEvent.eventAction.rawValue,
                eventCustomAction = if (tappEvent.eventAction is RequestModels.EventAction.Custom) {
                    tappEvent.eventAction.eventCustomAction
                } else {
                    "false"
                }
            )

            val result = tappService.trackEvent(tappEventRequest)

            withContext(Dispatchers.Main) {
                result.fold(
                    onSuccess = {
                        Logger.logInfo("Tapp event tracked successfully: ${tappEvent.eventName}")
                    },
                    onFailure = { error ->
                        Logger.logError("Failed to track Tapp event: ${error.localizedMessage}")
                    }
                )
            }
        }
    }


}

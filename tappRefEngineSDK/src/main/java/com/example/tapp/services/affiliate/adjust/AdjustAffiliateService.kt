package com.example.tapp.services.affiliate.adjust

import android.net.Uri
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustAdRevenue
import com.adjust.sdk.AdjustConfig
import com.adjust.sdk.AdjustDeeplink
import com.adjust.sdk.AdjustEvent
import com.adjust.sdk.AdjustPlayStorePurchase
import com.adjust.sdk.AdjustPurchaseVerificationResult
import com.adjust.sdk.LogLevel
import com.example.tapp.dependencies.Dependencies
import com.example.tapp.models.Environment
import com.example.tapp.services.affiliate.AffiliateService
import com.example.tapp.utils.Logger
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class AdjustAffiliateService(private val dependencies: Dependencies) : AffiliateService {
    private var isAdjustEnabled: Boolean = false

    override fun initialize(): Boolean {
        val context = dependencies.context
        val config = dependencies.keystoreUtils.getConfig()
        if (config == null) {
            Logger.logWarning("Error: Missing configuration")
            return false
        }

        return try {
            Logger.logInfo("Handling initialize Adjust")

            val adjustEnvironment = when (config.env) {
                Environment.PRODUCTION -> AdjustConfig.ENVIRONMENT_PRODUCTION
                Environment.SANDBOX    -> AdjustConfig.ENVIRONMENT_SANDBOX
            }

            val adjustConfig = AdjustConfig(context, config.appToken, adjustEnvironment)
            adjustConfig.setLogLevel(LogLevel.VERBOSE)
            println(adjustConfig);

            adjustConfig.setOnDeferredDeeplinkResponseListener { deeplink ->
                handleAdjustDeeplink(deeplink)
                // Returning false indicates that the deeplink is not consumed and should be processed further
                false
            }

            Adjust.initSdk(adjustConfig)
            Logger.logInfo("Adjust initialized and deeplink listener registered")
            true
        } catch (e: Exception) {
            Logger.logWarning("Error during Adjust referral processing: ${e.message}")
            false
        }
    }


    override fun handleCallback(deepLink: Uri) {
        val context = dependencies.context
        val url = AdjustDeeplink(deepLink)
        Adjust.processDeeplink(url, context)
        Logger.logInfo("Adjust notified of the incoming URL: $deepLink")
    }

    override fun handleEvent(eventId: String) {
        val adjustEvent = AdjustEvent(eventId)
        Adjust.trackEvent(adjustEvent)
        Logger.logInfo("Adjust tracked Event for event_id: $eventId")
    }

    override fun setEnabled(enabled: Boolean) {
        isAdjustEnabled = enabled
    }

    override fun isEnabled(): Boolean {
        return isAdjustEnabled
    }

    // MARK: - Monetization
    fun trackAdRevenue(source: String, revenue: Double, currency: String) {
        val adRevenue = AdjustAdRevenue(source).apply {
            setRevenue(revenue, currency)
        }
        Adjust.trackAdRevenue(adRevenue)
        Logger.logInfo("Tracked ad revenue for $source.")
    }

    fun verifyAppStorePurchase(
        transactionId: String,
        productId: String,
        completion: (AdjustPurchaseVerificationResult) -> Unit
    ) {
        val purchase = AdjustPlayStorePurchase(transactionId, productId)
        Adjust.verifyPlayStorePurchase(purchase) { result ->
            Logger.logInfo("Purchase verification result: $result")
            completion(result)
        }
    }

    // MARK: - Push Token
    fun setPushToken(token: String) {
        val context = dependencies.context
        Adjust.setPushToken(token, context)
        Logger.logInfo("Push token set: $token")
    }

    // MARK: - Device IDs
    fun getAdid(completion: (String?) -> Unit) {
        Adjust.getAdid { adid ->
            if (adid != null) {
                Logger.logInfo("ADID: $adid")
            } else {
                Logger.logError("No ADID available.")
            }
            completion(adid)
        }
    }

    fun getAdvertisingId(completion: (String?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val adInfo = AdvertisingIdClient.getAdvertisingIdInfo(dependencies.context)
                val adId = adInfo.id
                withContext(Dispatchers.Main) {
                    if (adId != null) {
                        Logger.logInfo("Advertising ID: $adId")
                    } else {
                        Logger.logError("No Advertising ID available.")
                    }
                    completion(adId)
                }
            } catch (e: Exception) {
                Logger.logError("Error fetching Advertising ID: ${e.localizedMessage}")
                withContext(Dispatchers.Main) {
                    completion(null)
                }
            }
        }
    }

    // MARK: - Internal Deep Link Handling
    private fun handleAdjustDeeplink(deepLink: Uri?) {
        if (deepLink != null) {
            Logger.logInfo("Received Adjust deeplink: $deepLink")
            // Call the internal appWillOpen method without a completion
            dependencies.tappInstance?.appWillOpenInt(deepLink.toString(), null) ?: run {
                Logger.logError("Tapp instance is not available to handle deeplink.")
            }
        } else {
            Logger.logWarning("Received null deeplink from Adjust.")
        }
    }

}

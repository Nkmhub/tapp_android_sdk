package com.example.tapp.services.affiliate.adjust

import android.content.Context
import android.net.Uri
import com.adjust.sdk.*
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

            // Register deferred deeplink listener.
            adjustConfig.setOnDeferredDeeplinkResponseListener { deeplink ->
                handleAdjustDeeplink(deeplink)
                // Returning false means that the deeplink is not consumed here.
                false
            }

            Adjust.initSdk(adjustConfig)
            Logger.logInfo("Adjust initialized and deeplink listener registered")
            true
        } catch (e: Exception) {
            Logger.logWarning("Error during Adjust initialization: ${e.message}")
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
        Logger.logInfo("Adjust tracked event for event_id: $eventId")
    }

    override fun setEnabled(enabled: Boolean) {
        isAdjustEnabled = enabled
    }

    override fun isEnabled(): Boolean {
        return isAdjustEnabled
    }

    // MARK: - Monetization & Purchases

    fun trackAdRevenue(source: String, revenue: Double, currency: String) {
        val adRevenue = AdjustAdRevenue(source).apply {
            setRevenue(revenue, currency)
        }
        Adjust.trackAdRevenue(adRevenue)
        Logger.logInfo("Tracked ad revenue for $source")
    }

    fun verifyAppStorePurchase(
        transactionId: String,
        productId: String,
        completion: (AdjustPurchaseVerificationResult) -> Unit
    ) {
        // In Android, this is implemented using Play Store purchase verification.
        val purchase = AdjustPlayStorePurchase(transactionId, productId)
        Adjust.verifyPlayStorePurchase(purchase) { result ->
            Logger.logInfo("Purchase verification result: $result")
            completion(result)
        }
    }

    fun verifyAndTrackPlayStorePurchase(
        event: AdjustEvent,
        listener: OnPurchaseVerificationFinishedListener
    ) {
        Adjust.verifyAndTrackPlayStorePurchase(event, listener)
        Logger.logInfo("verifyAndTrackPlayStorePurchase called for event: ${event.eventToken}")
    }

    // MARK: - Subscriptions

    fun trackPlayStoreSubscription(
        price: Long,
        currency: String,
        sku: String,
        orderId: String,
        signature: String,
        purchaseToken: String,
        purchaseTime: Long? = null
    ) {
        val playStoreSubscription = AdjustPlayStoreSubscription(
            price,      // Price of the subscription.
            currency,   // Currency code.
            sku,        // SKU / product identifier.
            orderId,    // Order identifier.
            signature,  // Purchase signature.
            purchaseToken  // Purchase token.
        )
        purchaseTime?.let {
            playStoreSubscription.setPurchaseTime(it)
        }
        Adjust.trackPlayStoreSubscription(playStoreSubscription)
        Logger.logInfo("Tracked Play Store subscription: orderId = $orderId")
    }

    // MARK: - Push Token

    fun setPushToken(token: String) {
        Adjust.setPushToken(token, dependencies.context)
        Logger.logInfo("Push token set: $token")
    }

    // MARK: - Device IDs & Advertising

    fun getAdid(completion: (String?) -> Unit) {
        Adjust.getAdid { adid ->
            if (adid != null) Logger.logInfo("ADID: $adid")
            else Logger.logError("No ADID available")
            completion(adid)
        }
    }

    fun getAdvertisingId(completion: (String?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val adInfo = AdvertisingIdClient.getAdvertisingIdInfo(dependencies.context)
                val adId = adInfo.id
                withContext(Dispatchers.Main) {
                    if (adId != null) Logger.logInfo("Advertising ID: $adId")
                    else Logger.logError("No Advertising ID available")
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

    fun getGoogleAdId(listener: OnGoogleAdIdReadListener) {
        Adjust.getGoogleAdId(dependencies.context, listener)
        Logger.logInfo("Requested Google Ad ID")
    }

    fun getAmazonAdId(listener: OnAmazonAdIdReadListener) {
        Adjust.getAmazonAdId(dependencies.context, listener)
        Logger.logInfo("Requested Amazon Ad ID")
    }

    fun getGooglePlayInstallReferrer(listener: OnGooglePlayInstallReferrerReadListener) {
        Adjust.getGooglePlayInstallReferrer(dependencies.context, listener)
        Logger.logInfo("Requested Google Play Install Referrer")
    }

    // MARK: - Attribution & Deeplinks

    fun getAdjustAttribution(completion: (AdjustAttribution?) -> Unit) {
        Adjust.getAttribution { attribution ->
            Logger.logInfo("Attribution received: $attribution")
            completion(attribution)
        }
        Logger.logInfo("Adjust getAttribution run")
    }

//    fun processAndResolveDeeplink(uri: Uri, listener: OnDeeplinkResolvedListener) {
//        val deeplink = AdjustDeeplink(uri)
//        Adjust.processAndResolveDeeplink(deeplink, dependencies.context, listener)
//        Logger.logInfo("processAndResolveDeeplink called with URI: $uri")
//    }

//    fun getLastDeeplink(listener: OnLastDeeplinkReadListener) {
//        Adjust.getLastDeeplink(dependencies.context, listener)
//        Logger.logInfo("Requested last deeplink")
//    }

    // MARK: - Lifecycle & Mode Switching

    fun onResume() {
        Adjust.onResume()
        Logger.logInfo("Adjust onResume called")
    }

    fun onPause() {
        Adjust.onPause()
        Logger.logInfo("Adjust onPause called")
    }

    fun enable() {
        Adjust.enable()
        Logger.logInfo("Adjust enabled")
    }

    fun disable() {
        Adjust.disable()
        Logger.logInfo("Adjust disabled")
    }

    fun isEnabled(context: Context, listener: OnIsEnabledListener) {
        Adjust.isEnabled(context, listener)
        Logger.logInfo("Adjust isEnabled check initiated")
    }

    fun switchToOfflineMode() {
        Adjust.switchToOfflineMode()
        Logger.logInfo("Switched to offline mode")
    }

    fun switchBackToOnlineMode() {
        Adjust.switchBackToOnlineMode()
        Logger.logInfo("Switched back to online mode")
    }

    // MARK: - Global Parameters

    fun addGlobalCallbackParameter(key: String, value: String) {
        Adjust.addGlobalCallbackParameter(key, value)
        Logger.logInfo("Added global callback parameter: $key = $value")
    }

    fun addGlobalPartnerParameter(key: String, value: String) {
        Adjust.addGlobalPartnerParameter(key, value)
        Logger.logInfo("Added global partner parameter: $key = $value")
    }

    fun removeGlobalCallbackParameter(key: String) {
        Adjust.removeGlobalCallbackParameter(key)
        Logger.logInfo("Removed global callback parameter: $key")
    }

    fun removeGlobalPartnerParameter(key: String) {
        Adjust.removeGlobalPartnerParameter(key)
        Logger.logInfo("Removed global partner parameter: $key")
    }

    fun removeGlobalCallbackParameters() {
        Adjust.removeGlobalCallbackParameters()
        Logger.logInfo("Removed all global callback parameters")
    }

    fun removeGlobalPartnerParameters() {
        Adjust.removeGlobalPartnerParameters()
        Logger.logInfo("Removed all global partner parameters")
    }

    // MARK: - Consent & Test Options

    fun trackMeasurementConsent(consent: Boolean) {
        Adjust.trackMeasurementConsent(consent)
        Logger.logInfo("Tracked measurement consent: $consent")
    }

    fun getSdkVersion(listener: OnSdkVersionReadListener) {
        Adjust.getSdkVersion(listener)
        Logger.logInfo("Requested SDK version")
    }

//    fun setTestOptions(options: AdjustTestOptions) {
//        Adjust.setTestOptions(options)
//        Logger.logInfo("Test options set: $options")
//    }

    // MARK: - Referrer

    fun setReferrer(referrer: String) {
        Adjust.setReferrer(referrer, dependencies.context)
        Logger.logInfo("setReferrer called with referrer: $referrer")
    }

    // MARK: - GDPR and Third-Party Sharing

    fun gdprForgetMe(context: Context?) {
        Adjust.gdprForgetMe(context)
        Logger.logInfo("Adjust gdprForgetMe run")
    }

    fun trackThirdPartySharing(isEnabled: Boolean) {
        val thirdPartySharing = AdjustThirdPartySharing(isEnabled)
        Adjust.trackThirdPartySharing(thirdPartySharing)
        Logger.logInfo("Adjust trackThirdPartySharing run with isEnabled: $isEnabled")
    }

    // MARK: - Internal Deeplink Handling

    private fun handleAdjustDeeplink(deepLink: Uri?) {
        if (deepLink != null) {
            Logger.logInfo("Received Adjust deeplink: $deepLink")
            dependencies.tappInstance?.appWillOpenInt(deepLink.toString(), null) ?: run {
                Logger.logError("Tapp instance is not available to handle deeplink.")
            }
        } else {
            Logger.logWarning("Received null deeplink from Adjust.")
        }
    }
}

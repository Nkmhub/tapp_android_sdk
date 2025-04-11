package com.example.tapp

import android.content.Context
import com.adjust.sdk.AdjustEvent
import com.adjust.sdk.AdjustPlayStorePurchase
import com.adjust.sdk.AdjustPurchaseVerificationResult
import com.adjust.sdk.AdjustTestOptions
import com.adjust.sdk.OnAmazonAdIdReadListener
import com.adjust.sdk.OnGoogleAdIdReadListener
import com.adjust.sdk.OnGooglePlayInstallReferrerReadListener
import com.adjust.sdk.OnIsEnabledListener
import com.adjust.sdk.OnPurchaseVerificationFinishedListener
import com.adjust.sdk.OnSdkVersionReadListener
import com.example.tapp.services.affiliate.adjust.AdjustAffiliateService
import com.example.tapp.utils.Logger

val Tapp.context: Context
    get() = dependencies.context

fun Tapp.adjustTrackAdRevenue(source: String, revenue: Double, currency: String) {
    getAdjustService()?.trackAdRevenue(source, revenue, currency)
        ?: Logger.logError("Adjust service not available.")
}

fun Tapp.adjustVerifyAppStorePurchase(
    transactionId: String,
    productId: String,
    completion: (AdjustPurchaseVerificationResult) -> Unit
) {
    getAdjustService()?.verifyAppStorePurchase(transactionId, productId, completion)
        ?: Logger.logError("Adjust service not available.")
}

fun Tapp.adjustSetPushToken(token: String) {
    getAdjustService()?.setPushToken(token)
        ?: Logger.logError("Adjust service not available.")
}

fun Tapp.adjustGdprForgetMe() {
    getAdjustService()?.gdprForgetMe(context)
        ?: Logger.logError("Adjust service not available.")
}

fun Tapp.adjustTrackThirdPartySharing(isEnabled: Boolean) {
    getAdjustService()?.trackThirdPartySharing(isEnabled)
        ?: Logger.logError("Adjust service not available.")
}

fun Tapp.getAdjustAttribution(completion: (com.adjust.sdk.AdjustAttribution?) -> Unit) {
    getAdjustService()?.getAdjustAttribution(completion)
        ?: Logger.logError("Adjust service not available.")
}

fun Tapp.adjustGetAdid(completion: (String?) -> Unit) {
    getAdjustService()?.getAdid(completion)
        ?: Logger.logError("Adjust service not available.")
}

fun Tapp.adjustGetIdfa(completion: (String?) -> Unit) {
    getAdjustService()?.getAdvertisingId(completion)
        ?: Logger.logError("Adjust service not available.")
}

fun Tapp.adjustTrackPlayStoreSubscription(
    price: Long,
    currency: String,
    sku: String,
    orderId: String,
    signature: String,
    purchaseToken: String,
    purchaseTime: Long? = null
) {
    getAdjustService()?.trackPlayStoreSubscription(
        price,
        currency,
        sku,
        orderId,
        signature,
        purchaseToken,
        purchaseTime
    ) ?: Logger.logError("Adjust service not available.")
}

fun Tapp.adjustVerifyAndTrackPlayStorePurchase(
    event: AdjustEvent,
    listener: OnPurchaseVerificationFinishedListener
) {
    getAdjustService()?.verifyAndTrackPlayStorePurchase(event, listener)
        ?: Logger.logError("Adjust service not available.")
}

fun Tapp.verifyPlayStorePurchase(
    purchase: AdjustPlayStorePurchase,
    listener: OnPurchaseVerificationFinishedListener
) {
    getAdjustService()?.verifyPlayStorePurchase(purchase, listener)
        ?: Logger.logError("Adjust service not available.")
}

fun Tapp.adjustGetGoogleAdId(listener: OnGoogleAdIdReadListener) {
    getAdjustService()?.getGoogleAdId(listener)
        ?: Logger.logError("Adjust service not available.")
}

fun Tapp.adjustGetAmazonAdId(listener: OnAmazonAdIdReadListener) {
    getAdjustService()?.getAmazonAdId(listener)
        ?: Logger.logError("Adjust service not available.")
}

fun Tapp.adjustGetGooglePlayInstallReferrer(listener: OnGooglePlayInstallReferrerReadListener) {
    getAdjustService()?.getGooglePlayInstallReferrer(listener)
        ?: Logger.logError("Adjust service not available.")
}

//fun Tapp.adjustProcessAndResolveDeeplink(uri: Uri, listener: OnDeeplinkResolvedListener) {
//    getAdjustService()?.processAndResolveDeeplink(uri, listener)
//        ?: Logger.logError("Adjust service not available.")
//}

//fun Tapp.adjustGetLastDeeplink(listener: OnLastDeeplinkReadListener) {
//    getAdjustService()?.getLastDeeplink(listener)
//        ?: Logger.logError("Adjust service not available.")
//}

fun Tapp.adjustOnResume() {
    getAdjustService()?.onResume() ?: Logger.logError("Adjust service not available.")
}

fun Tapp.adjustOnPause() {
    getAdjustService()?.onPause() ?: Logger.logError("Adjust service not available.")
}

fun Tapp.adjustEnable() {
    getAdjustService()?.enable() ?: Logger.logError("Adjust service not available.")
}

fun Tapp.adjustDisable() {
    getAdjustService()?.disable() ?: Logger.logError("Adjust service not available.")
}

fun Tapp.adjustIsEnabled(context: Context, listener: OnIsEnabledListener) {
    getAdjustService()?.isEnabled(context, listener)
        ?: Logger.logError("Adjust service not available.")
}

fun Tapp.adjustSwitchToOfflineMode() {
    getAdjustService()?.switchToOfflineMode() ?: Logger.logError("Adjust service not available.")
}

fun Tapp.adjustSwitchBackToOnlineMode() {
    getAdjustService()?.switchBackToOnlineMode() ?: Logger.logError("Adjust service not available.")
}

fun Tapp.adjustAddGlobalCallbackParameter(key: String, value: String) {
    getAdjustService()?.addGlobalCallbackParameter(key, value)
        ?: Logger.logError("Adjust service not available.")
}

fun Tapp.adjustAddGlobalPartnerParameter(key: String, value: String) {
    getAdjustService()?.addGlobalPartnerParameter(key, value)
        ?: Logger.logError("Adjust service not available.")
}

fun Tapp.adjustRemoveGlobalCallbackParameter(key: String) {
    getAdjustService()?.removeGlobalCallbackParameter(key)
        ?: Logger.logError("Adjust service not available.")
}

fun Tapp.adjustRemoveGlobalPartnerParameter(key: String) {
    getAdjustService()?.removeGlobalPartnerParameter(key)
        ?: Logger.logError("Adjust service not available.")
}

fun Tapp.adjustRemoveGlobalCallbackParameters() {
    getAdjustService()?.removeGlobalCallbackParameters()
        ?: Logger.logError("Adjust service not available.")
}

fun Tapp.adjustRemoveGlobalPartnerParameters() {
    getAdjustService()?.removeGlobalPartnerParameters()
        ?: Logger.logError("Adjust service not available.")
}

fun Tapp.adjustTrackMeasurementConsent(consent: Boolean) {
    getAdjustService()?.trackMeasurementConsent(consent)
        ?: Logger.logError("Adjust service not available.")
}

fun Tapp.adjustGetSdkVersion(listener: OnSdkVersionReadListener) {
    getAdjustService()?.getSdkVersion(listener)
        ?: Logger.logError("Adjust service not available.")
}

//fun Tapp.adjustSetTestOptions(options: AdjustTestOptions) {
//    getAdjustService()?.setTestOptions(options)
//        ?: Logger.logError("Adjust service not available.")
//}

fun Tapp.adjustSetReferrer(referrer: String) {
    getAdjustService()?.setReferrer(referrer)
        ?: Logger.logError("Adjust service not available.")
}


// Utility to retrieve AdjustAffiliateService
private fun Tapp.getAdjustService(): AdjustAffiliateService? {
    val affiliateService = dependencies.affiliateServiceFactory.getAffiliateService(
        dependencies.keystoreUtils.getConfig()?.affiliate ?: return null,
        dependencies
    )
    return if (affiliateService is AdjustAffiliateService) affiliateService else null
}

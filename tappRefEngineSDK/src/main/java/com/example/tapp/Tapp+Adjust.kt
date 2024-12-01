package com.example.tapp
import com.adjust.sdk.AdjustPurchaseVerificationResult
import com.example.tapp.services.affiliate.adjust.AdjustAffiliateService
import com.example.tapp.utils.Logger

fun Tapp.adjustTrackAdRevenue(source: String, revenue: Double, currency: String) {
    getAdjustService()?.trackAdRevenue(source, revenue, currency) ?: Logger.logError("Adjust service not available.")
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
    getAdjustService()?.setPushToken(token) ?: Logger.logError("Adjust service not available.")
}

fun Tapp.adjustGetAdid(completion: (String?) -> Unit) {
    getAdjustService()?.getAdid(completion) ?: Logger.logError("Adjust service not available.")
}

fun Tapp.adjustGetIdfa(completion: (String?) -> Unit) {
    getAdjustService()?.getAdvertisingId(completion) ?: Logger.logError("Adjust service not available.")
}

// Utility to retrieve AdjustAffiliateService
private fun Tapp.getAdjustService(): AdjustAffiliateService? {
    val affiliateService = dependencies.affiliateServiceFactory.getAffiliateService(
        dependencies.keystoreUtils.getConfig()?.affiliate ?: return null,
        dependencies
    )
    return if (affiliateService is AdjustAffiliateService) affiliateService else null
}

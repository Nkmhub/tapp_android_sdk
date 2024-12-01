package com.example.tapp.utils

import com.example.tapp.models.Affiliate
import com.example.tapp.models.Environment
import kotlinx.serialization.Serializable

//@Serializable
data class TappConfiguration(
    val authToken: String,
    val env: Environment,
    val tappToken: String,
    val affiliate: Affiliate,
    val bundleID: String? = null,
    private var appToken: String? = null,
    private var hasProcessedReferralEngine: Boolean = false,
    val androidId: String? = null
) {
    fun setAppToken(appToken: String) {
        this.appToken = appToken
    }

    fun getAppToken(): String? {
        return appToken
    }

    // Getter for hasProcessedReferralEngine
    fun isReferralEngineProcessed(): Boolean {
        return hasProcessedReferralEngine
    }

    // Setter for hasProcessedReferralEngine
    fun setReferralEngineProcessed(value: Boolean) {
        hasProcessedReferralEngine = value
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TappConfiguration) return false

        val equalNonOptionalValues = authToken == other.authToken &&
                env == other.env &&
                tappToken == other.tappToken &&
                affiliate == other.affiliate &&
                androidId == other.androidId

        val appTokensEqual = appToken == other.appToken
        return equalNonOptionalValues && appTokensEqual
    }

    override fun hashCode(): Int {
        return authToken.hashCode() * 31 +
                env.hashCode() +
                tappToken.hashCode() +
                affiliate.hashCode()+
                (androidId?.hashCode() ?: 0)
    }
}

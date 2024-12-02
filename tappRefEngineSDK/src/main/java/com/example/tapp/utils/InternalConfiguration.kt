package com.example.tapp.utils

import com.example.tapp.models.Affiliate
import com.example.tapp.models.Environment
import kotlinx.serialization.Serializable

@Serializable
internal data class InternalConfiguration(
    val authToken: String,
    val env: Environment,
    val tappToken: String,
    val affiliate: Affiliate,
    val bundleID: String,
    var appToken: String? = null,
    var hasProcessedReferralEngine: Boolean = false,
    val androidId: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is InternalConfiguration) return false

        return authToken == other.authToken &&
                env == other.env &&
                tappToken == other.tappToken &&
                affiliate == other.affiliate &&
                bundleID == other.bundleID &&
                appToken == other.appToken &&
                hasProcessedReferralEngine == other.hasProcessedReferralEngine &&
                androidId == other.androidId
    }

    override fun hashCode(): Int {
        var result = authToken.hashCode()
        result = 31 * result + env.hashCode()
        result = 31 * result + tappToken.hashCode()
        result = 31 * result + affiliate.hashCode()
        result = 31 * result + bundleID.hashCode()
        result = 31 * result + (appToken?.hashCode() ?: 0)
        result = 31 * result + hasProcessedReferralEngine.hashCode()
        result = 31 * result + androidId.hashCode()
        return result
    }
}

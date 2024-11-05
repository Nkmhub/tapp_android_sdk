package com.example.tapprefenginesdk.services.affiliate

import android.content.Context
import com.example.tapprefenginesdk.models.Affiliate
import com.example.tapprefenginesdk.models.AffiliateUrlResponse
import com.example.tapprefenginesdk.models.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface AffiliateService {
    fun processReferral(
        context: Context,
        deepLink: String,
        environment: Environment,
        appToken: String
    ):Boolean
    fun handleCallback(
        context: Context,
        deepLink: String,
    )

    suspend fun handleEvent(eventId:String,authToken:String?)
    suspend fun generateAffiliateUrl(
        wreToken: String,
        influencer: String,
        adgroup: String,
        creative: String,
        mmp: Affiliate,
        token: String,
        jsonObject: Map<String, Any>
    ): AffiliateUrlResponse

}

package com.example.wre_ref_engine_sdk.Services.Affiliate.AppFlyer

import android.content.Context
import com.example.wre_ref_engine_sdk.Models.AdjustEnv
import com.example.wre_ref_engine_sdk.Services.Affiliate.AffiliateService

class AppsflyerAffiliateService : AffiliateService {
    override fun processReferral(
        context: Context,
        deepLink: String,
        environment: AdjustEnv,
        appToken: String
    ) {
        println("Handling AppFlyer referral")
        println("App Token: $appToken")
        println("Deep Link: $deepLink")

        // TODO: Add your AppFlyer-specific logic here
    }
}

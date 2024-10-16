package com.example.wre_ref_engine_sdk.Services.Affiliate

import android.content.Context
import com.example.wre_ref_engine_sdk.Models.AdjustEnv

interface AffiliateService {
    fun processReferral(
        context: Context,
        deepLink: String,
        environment: AdjustEnv,
        appToken: String
    )
}

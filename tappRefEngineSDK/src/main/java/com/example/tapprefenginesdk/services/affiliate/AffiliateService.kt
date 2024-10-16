package com.example.tapprefenginesdk.services.affiliate

import android.content.Context
import com.example.tapprefenginesdk.models.AdjustEnv

interface AffiliateService {
    fun processReferral(
        context: Context,
        deepLink: String,
        environment: AdjustEnv,
        appToken: String
    )
}

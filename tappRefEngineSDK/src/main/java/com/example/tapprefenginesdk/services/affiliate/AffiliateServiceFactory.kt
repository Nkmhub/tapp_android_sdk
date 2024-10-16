package com.example.tapprefenginesdk.services.affiliate

import com.example.tapprefenginesdk.models.Affiliate
import com.example.tapprefenginesdk.services.affiliate.AffiliateService
import com.example.wre_ref_engine_sdk.Services.Affiliate.Adjust.AdjustAffiliateService
import com.example.wre_ref_engine_sdk.Services.Affiliate.AppFlyer.AppsflyerAffiliateService

object AffiliateServiceFactory {
    fun getAffiliateService(affiliate: Affiliate): AffiliateService? {
        return when (affiliate) {
            Affiliate.ADJUST -> AdjustAffiliateService()
            Affiliate.APPFLYER -> AppsflyerAffiliateService()
        }
    }
}

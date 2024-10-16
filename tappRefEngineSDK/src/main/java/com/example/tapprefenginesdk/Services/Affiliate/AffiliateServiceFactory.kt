package com.example.wre_ref_engine_sdk.Services.Affiliate

import com.example.wre_ref_engine_sdk.Models.Affiliate
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

package com.example.tapprefenginesdk.services.affiliate

import com.example.tapprefenginesdk.models.Affiliate
import com.example.tapprefenginesdk.services.affiliate.adjust.AdjustAffiliateService
import com.example.tapprefenginesdk.services.affiliate.appFlyer.AppsflyerAffiliateService
import com.example.tapprefenginesdk.services.affiliate.tapp.TappAffiliateService

object AffiliateServiceFactory {
    fun getAffiliateService(affiliate: Affiliate): AffiliateService? {
        return when (affiliate) {
            Affiliate.ADJUST -> AdjustAffiliateService()
            Affiliate.APPFLYER -> AppsflyerAffiliateService()
            Affiliate.TAPP -> TappAffiliateService()
        }
    }
}

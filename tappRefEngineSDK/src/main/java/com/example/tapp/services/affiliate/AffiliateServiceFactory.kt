package com.example.tapp.services.affiliate

import com.example.tapp.dependencies.Dependencies
import com.example.tapp.models.Affiliate
import com.example.tapp.services.affiliate.adjust.AdjustAffiliateService
import com.example.tapp.services.affiliate.appFlyer.AppsflyerAffiliateService
import com.example.tapp.services.affiliate.tapp.TappAffiliateService

internal object AffiliateServiceFactory {
    fun getAffiliateService(affiliate: Affiliate, dependencies: Dependencies): AffiliateService? {
        return when (affiliate) {
            Affiliate.ADJUST -> AdjustAffiliateService(dependencies) // Adjust does not depend on Dependencies for now
            Affiliate.APPFLYER -> AppsflyerAffiliateService(dependencies) // AppFlyer also doesn't depend for now
            Affiliate.TAPP -> TappAffiliateService(dependencies) // Pass NetworkManager
        }
    }
}

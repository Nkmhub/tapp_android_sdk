package com.example.tapp.services.affiliate.appFlyer

import com.example.tapp.dependencies.Dependencies
import com.example.tapp.services.affiliate.AffiliateService
import com.example.tapp.utils.Logger

class AppsflyerAffiliateService(private val dependencies: Dependencies) : AffiliateService {
    override fun initialize(): Boolean {
        TODO("APPFLYER: Add your AppFlyer-specific logic here")
        Logger.logWarning("APPFLYER_initialize: Not yet implemented")
        return false;
    }

    override fun handleCallback(deepLink: String) {
        TODO("APPFLYER: Not yet implemented")
        Logger.logWarning("APPFLYER_handleCallback: Not yet implemented")
    }

    override fun handleEvent(eventId: String) {
        TODO("APPFLYER: Not yet implemented")
        Logger.logWarning("APPFLYER: Not yet implemented: $eventId")
    }

}

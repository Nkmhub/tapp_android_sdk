package com.example.tapp.services.affiliate.appFlyer

import android.net.Uri
import com.example.tapp.dependencies.Dependencies
import com.example.tapp.services.affiliate.AffiliateService
import com.example.tapp.utils.Logger

class AppsflyerAffiliateService(private val dependencies: Dependencies) : AffiliateService {
    private var isAppflyerEnabled: Boolean = true // Default value

    override fun initialize(): Boolean {
        TODO("APPFLYER: Add your AppFlyer-specific logic here")
        Logger.logWarning("APPFLYER_initialize: Not yet implemented")
        return false;
    }

    override fun handleCallback(deepLink: Uri) {
        TODO("APPFLYER: Not yet implemented")
        Logger.logWarning("APPFLYER_handleCallback: Not yet implemented")
    }

    override fun handleEvent(eventId: String) {
        TODO("APPFLYER: Not yet implemented")
        Logger.logWarning("APPFLYER: Not yet implemented: $eventId")
    }

    override fun isEnabled(): Boolean {
        return isAppflyerEnabled
    }

    override fun setEnabled(enabled: Boolean) {
        isAppflyerEnabled = enabled
    }

}

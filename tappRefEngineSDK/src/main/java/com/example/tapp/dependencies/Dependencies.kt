package com.example.tapp.dependencies

import android.content.Context
import com.example.tapp.services.network.NetworkManager
import com.example.tapp.services.affiliate.AffiliateServiceFactory
import com.example.tapp.utils.KeystoreUtils

internal class Dependencies private constructor(
    val context: Context,
    val keystoreUtils: KeystoreUtils,
    val networkManager: NetworkManager,
    val affiliateServiceFactory: AffiliateServiceFactory
) {
    companion object {
        private var instance: Dependencies? = null

        // Live instance for production
        fun live(context: Context): Dependencies {
            if (instance == null) {
                val keystoreUtils = KeystoreUtils(context)
                val networkManager = NetworkManager()
                val affiliateServiceFactory = AffiliateServiceFactory

                instance = Dependencies(
                    context = context,
                    keystoreUtils = keystoreUtils,
                    networkManager = networkManager,
                    affiliateServiceFactory = affiliateServiceFactory
                )
            }
            return instance!!
        }
    }
}

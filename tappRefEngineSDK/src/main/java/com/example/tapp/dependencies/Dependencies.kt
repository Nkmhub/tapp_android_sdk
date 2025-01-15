package com.example.tapp.dependencies

import android.content.Context
import com.example.tapp.Tapp
import com.example.tapp.services.network.NetworkManager
import com.example.tapp.services.affiliate.AffiliateServiceFactory
import com.example.tapp.utils.KeystoreUtils

internal class Dependencies private constructor(
    val context: Context,
    val keystoreUtils: KeystoreUtils,
    val networkManager: NetworkManager,
    val affiliateServiceFactory: AffiliateServiceFactory,
    var tappInstance: Tapp? = null // Added reference to Tapp
) {
    companion object {
        @Volatile
        private var instance: Dependencies? = null

        // Live instance for production
        fun live(context: Context): Dependencies {
            return instance ?: synchronized(this) {
                instance ?: Dependencies(
                    context = context,
                    keystoreUtils = KeystoreUtils(context),
                    networkManager = NetworkManager(),
                    affiliateServiceFactory = AffiliateServiceFactory
                ).also { instance = it }
            }
        }
    }
}

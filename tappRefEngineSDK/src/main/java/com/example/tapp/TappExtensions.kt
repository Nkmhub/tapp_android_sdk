package com.example.tapp

import android.net.Uri
import com.example.tapp.models.Affiliate
import com.example.tapp.services.affiliate.tapp.TappAffiliateService
import com.example.tapp.services.network.RequestModels
import com.example.tapp.services.network.TappError
import com.example.tapp.utils.Logger
import com.example.tapp.utils.TappConfiguration
import com.example.tapp.utils.VoidCompletion


internal fun Tapp.appWillOpen(url: Uri, completion: VoidCompletion?) {
    fetchSecretsAndInitializeReferralEngineIfNeeded { result ->
        result.fold(
            onSuccess = {
                handleReferralCallback(url, completion)
            },
            onFailure = { error ->
                completion?.invoke(Result.failure(error))
            }
        )
    }
}

internal fun Tapp.handleReferralCallback(
    url: Uri,
    completion: VoidCompletion?
) {
    // Step 1: Use Tapp service for handleImpression
    val tappService =
        dependencies.affiliateServiceFactory.getAffiliateService(Affiliate.TAPP, dependencies)
    if (tappService !is TappAffiliateService) {
        completion?.invoke(Result.failure(TappError.MissingAffiliateService("Affiliate service not available for tappService")))
        return
    }

    tappService.handleImpression(url) { result ->
        result.fold(
            onSuccess = { tappUrlResponse ->
                Logger.logInfo("start handleImpression with result: $tappUrlResponse")

                val affiliateService = dependencies.affiliateServiceFactory.getAffiliateService(
                    dependencies.keystoreUtils.getConfig()?.affiliate ?: Affiliate.TAPP,
                    dependencies
                )

                if (affiliateService == null) {
                    completion?.invoke(Result.failure(TappError.MissingAffiliateService("Affiliate service not available")))
                    return@fold
                }

                // Check if error is true before calling handleCallback
                if (!tappUrlResponse.error) {
                    // Only call handleCallback if error == false
                    affiliateService.handleCallback(url)
                }

                // Always run these two calls regardless of error state
                saveDeepLinkUrl(url.toString())
                setProcessedReferralEngine()

                completion?.invoke(Result.success(Unit))
            },
            onFailure = { error ->
                completion?.invoke(
                    Result.failure(
                        TappError.affiliateErrorResult(
                            error,
                            Affiliate.TAPP
                        )
                    )
                )
            }
        )
    }

}

internal fun Tapp.fetchSecretsAndInitializeReferralEngineIfNeeded(completion: VoidCompletion?) {
    val config = dependencies.keystoreUtils.getConfig()
    if (config == null) {
        completion?.invoke(Result.failure(TappError.MissingConfiguration()))
        return
    }

    secrets() { result: Result<Unit> ->
        result.fold(
            onSuccess = {
                initializeAffiliateService(completion)
            },
            onFailure = { error ->
                completion?.invoke(
                    Result.failure(TappError.affiliateErrorResult(error, config.affiliate))
                )
            }
        )
    }
}

internal fun Tapp.secrets(completion: (Result<Unit>) -> Unit) {
    val storedConfig = dependencies.keystoreUtils.getConfig()
    if (storedConfig == null) {
        completion(Result.failure(TappError.MissingConfiguration()))
        return
    }

    if (storedConfig.appToken != null) {
        completion(Result.success(Unit))
        return
    }

    val tappService =
        dependencies.affiliateServiceFactory.getAffiliateService(Affiliate.TAPP, dependencies)
    if (tappService is TappAffiliateService) {
        tappService.fetchSecrets() { result: Result<RequestModels.SecretsResponse> ->
            result.fold(
                onSuccess = { response ->
                    storedConfig.appToken = response.secret
                    dependencies.keystoreUtils.saveConfig(storedConfig)
                    completion(Result.success(Unit))
                },
                onFailure = { error ->
                    completion(Result.failure(error))
                }
            )
        }
    } else {
        completion(Result.failure(TappError.MissingAffiliateService("Affiliate service not available")))
    }
}


internal fun Tapp.initializeAffiliateService(completion: VoidCompletion?) {
    val config = dependencies.keystoreUtils.getConfig()
    if (config == null) {
        completion?.invoke(Result.failure(TappError.MissingConfiguration()))
        return
    }

    val affiliateService = dependencies.affiliateServiceFactory.getAffiliateService(
        config.affiliate,
        dependencies
    )

    if (affiliateService == null) {
        completion?.invoke(Result.failure(TappError.MissingAffiliateService("Affiliate service not available")))
        return
    }

    if (affiliateService.isEnabled()) {
        Logger.logInfo("Affiliate service is already enabled. Skipping initialization.")
        completion?.invoke(Result.success(Unit))
        return
    }

    val success = affiliateService.initialize()

    if (success) {
        affiliateService.setEnabled(true)
        completion?.invoke(Result.success(Unit))
    } else {
        affiliateService.setEnabled(false)
        completion?.invoke(
            Result.failure(
                TappError.InitializationFailed(
                    details = "Affiliate service initialization failed."
                )
            )
        )
    }
}

internal fun Tapp.saveDeepLinkUrl(deepLinkUrl: String?) {
    if (deepLinkUrl.isNullOrBlank()) {
        Logger.logWarning("Cannot save deep link URL: URL is null or blank")
        return
    }

    Logger.logInfo("Saving deep link URL: $deepLinkUrl")
    val config = dependencies.keystoreUtils.getConfig()
    if (config != null) {
        val updatedConfig = config.copy(deepLinkUrl = deepLinkUrl)
        dependencies.keystoreUtils.saveConfig(updatedConfig)
        Logger.logInfo("Deep link URL saved: $deepLinkUrl")
    } else {
        Logger.logError("Failed to save deep link URL: configuration is null")
    }
}



internal fun Tapp.setProcessedReferralEngine() {
    val config = dependencies.keystoreUtils.getConfig()
    if (config != null) {
        val updatedConfig = config.copy(hasProcessedReferralEngine = true)
        dependencies.keystoreUtils.saveConfig(updatedConfig)
        Logger.logInfo("Updated hasProcessedReferralEngine to true in config: $updatedConfig")
    } else {
        Logger.logWarning("Cannot set hasProcessedReferralEngine to true: config is null")
    }
}


internal fun Tapp.hasProcessedReferralEngine(): Boolean {
    return dependencies.keystoreUtils.getConfig()?.hasProcessedReferralEngine
        ?: false // Directly access the property
}




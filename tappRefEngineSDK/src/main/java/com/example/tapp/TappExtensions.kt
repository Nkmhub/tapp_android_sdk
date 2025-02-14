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
    val config = dependencies.keystoreUtils.getConfig()
    if (config == null) {
        // No config -> cannot proceed
        completion?.invoke(Result.failure(TappError.MissingConfiguration()))
        return
    }

    // Check if we already have secrets and the service is enabled
    val service = dependencies.affiliateServiceFactory
        .getAffiliateService(config.affiliate, dependencies)

    val hasSecrets = config.appToken != null
    val isServiceEnabled = service?.isEnabled() == true

    // If we ALREADY have secrets + the service is enabled,
    // skip re-fetching secrets or re-initializing,
    // and go straight to handleReferralCallback:
    if (hasSecrets && isServiceEnabled) {
        handleReferralCallback(url, completion)
        return
    }

    // Otherwise, secrets or init are still needed:
    fetchSecretsAndInitializeReferralEngineIfNeeded { result ->
        result.fold(
            onSuccess = {
                // Now that secrets & init are done, handle the callback
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

                // Extract a specific parameter from the URL before saving the deep link URL
                val linkToken = url.getQueryParameter("adj_t")
                if (linkToken != null) {
                    Logger.logInfo("Extracted linkToken: $linkToken")
                }

                // Always run these two calls regardless of error state
                saveDeepLinkUrl(url.toString())
                saveLinkToken(linkToken)
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

internal fun Tapp.fetchSecretsAndInitializeReferralEngineIfNeeded(
    completion: (Result<Unit>) -> Unit
) {
    val config = dependencies.keystoreUtils.getConfig()
    if (config == null) {
        completion(Result.failure(TappError.MissingConfiguration()))
        return
    }

    // If we already have an appToken and service is enabled, skip
    val hasSecrets = (config.appToken != null)
    val service = dependencies.affiliateServiceFactory
        .getAffiliateService(config.affiliate, dependencies)
    val isEnabled = service?.isEnabled() == true

    if (hasSecrets && isEnabled) {
        completion(Result.success(Unit))
        return
    }

    // Otherwise fetch secrets
    secrets { secretResult ->
        secretResult.fold(
            onSuccess = {
                // then init
                initializeAffiliateService { initResult ->
                    completion(initResult)
                }
            },
            onFailure = { error ->
                completion(Result.failure(error))
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

internal fun Tapp.saveLinkToken(linkToken: String?) {
    if (linkToken.isNullOrBlank()) {
        Logger.logWarning("Cannot save linkToken: linkToken is null or blank")
        return
    }

    Logger.logInfo("Saving linkToken: $linkToken")
    val config = dependencies.keystoreUtils.getConfig()
    if (config != null) {
        val updatedConfig = config.copy(linkToken = linkToken)
        dependencies.keystoreUtils.saveConfig(updatedConfig)
        Logger.logInfo("linkToken saved: $linkToken")
    } else {
        Logger.logError("Failed to save linkToken: configuration is null")
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




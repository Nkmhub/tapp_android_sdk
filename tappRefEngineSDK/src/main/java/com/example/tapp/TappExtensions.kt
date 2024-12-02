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
            onSuccess = {
                // Step 2: Use user's configured affiliate service for handleCallback
                val affiliateService = dependencies.affiliateServiceFactory.getAffiliateService(
                    dependencies.keystoreUtils.getConfig()?.affiliate ?: Affiliate.TAPP,
                    dependencies
                )

                if (affiliateService == null) {
                    completion?.invoke(Result.failure(TappError.MissingAffiliateService("Affiliate service not available")))
                    return@fold
                }

                affiliateService.handleCallback(url) // Call handleCallback on user's affiliate service
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
    val affiliateService = dependencies.affiliateServiceFactory.getAffiliateService(
        dependencies.keystoreUtils.getConfig()?.affiliate ?: Affiliate.TAPP,
        dependencies
    )
    if (affiliateService == null) {
        completion?.invoke(Result.failure(TappError.MissingAffiliateService("Affiliate service not available")))
        return
    }

    if (affiliateService.isEnabled()) {
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



internal fun Tapp.setProcessedReferralEngine() {
    val storedConfig = dependencies.keystoreUtils.getConfig()
    storedConfig?.let {
        it.hasProcessedReferralEngine = true // Directly set the property
        dependencies.keystoreUtils.saveConfig(it)
        Logger.logInfo("Updated hasProcessedReferralEngine to true in config: $it")

    }
}

internal fun Tapp.hasProcessedReferralEngine(): Boolean {
    return dependencies.keystoreUtils.getConfig()?.hasProcessedReferralEngine
        ?: false // Directly access the property
}


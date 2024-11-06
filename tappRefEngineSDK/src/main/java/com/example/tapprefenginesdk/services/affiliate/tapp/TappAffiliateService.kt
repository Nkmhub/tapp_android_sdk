package com.example.tapprefenginesdk.services.affiliate.tapp

import android.content.Context
import com.example.tapprefenginesdk.models.Affiliate
import com.example.tapprefenginesdk.models.AffiliateUrlResponse
import com.example.tapprefenginesdk.models.Environment
import com.example.tapprefenginesdk.services.affiliate.AffiliateService
import com.example.tapprefenginesdk.services.network.NetworkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class TappAffiliateService: AffiliateService {
    private val baseAPIURL = "https://www.nkmhub.com/api/wre/"
    private val networkManager = NetworkManager()

    override fun processReferral(
        context: Context,
        deepLink: String,
        environment: Environment,
        appToken: String
    ): Boolean {
        TODO("TAPP: Not yet implemented")
    }

    override fun handleCallback(context: Context, deepLink: String) {
        TODO("TAPP: Not yet implemented")
    }

    override suspend fun handleEvent(eventId: String, authToken: String?) {
        // Check if the authToken is valid
        if (authToken.isNullOrEmpty()) {
            println("Error: authToken shouldn't be empty.")
            return
        }

        println("Handling Tapp callback for events with ID: $eventId")

        val apiURL = "${baseAPIURL}event"
        val requestBody = mapOf(
            "event_name" to eventId
        )

        val headers = mapOf(
            "Authorization" to "Bearer $authToken"
        )

        // Send the POST request
        val networkManager = NetworkManager()

        val result = networkManager.postRequest(
            url = apiURL,
            params = requestBody,
            headers = headers
        )

        result.onSuccess { jsonResponse ->
            println("Event tracked successfully: $jsonResponse")
        }.onFailure { exception ->
            println("Failed to track event: ${exception.localizedMessage}")
        }
    }

    override suspend fun generateAffiliateUrl(
        wreToken: String,
        influencer: String,
        adgroup: String,
        creative: String,
        mmp: Affiliate,
        token: String,
        jsonObject: Map<String, Any>
    ): AffiliateUrlResponse = withContext(Dispatchers.IO) {
        val apiUrl = "${baseAPIURL}generateUrl"

        println("Starting generateAffiliateUrl...")

        // Prepare the request body with nested JSON for "data"
        val requestBody = mapOf(
            "wre_token" to wreToken,
            "mmp" to mmp.toString(),
            "influencer" to influencer,
            "adgroup" to adgroup,
            "creative" to creative,
            "data" to jsonObject // Pass as a Map directly for nested JSON
        )

        // Set headers
        val headers = mapOf(
            "Authorization" to "Bearer $token",
            "Content-Type" to "application/json"
        )

        // Make the network request and handle the result
        try {
            val result = networkManager.postRequest(apiUrl, requestBody, headers)

            result.fold(
                onSuccess = { jsonResponse ->
                    println("Successfully received response from networkManager.postRequest")
                    println("Raw JSON Response: $jsonResponse")

                    val error = jsonResponse.optBoolean("error", true)
                    val message = jsonResponse.optString("message", "Unknown error")
                    val affiliateUrl = jsonResponse.optString("influencer_url", "")

                    println("Parsed Response - Error: $error, Message: $message, Affiliate URL: $affiliateUrl")

                    AffiliateUrlResponse(
                        error = error,
                        message = message,
                        influencer_url = affiliateUrl
                    )
                },
                onFailure = { exception ->
                    println("Error in network request: ${exception.localizedMessage}")
                    AffiliateUrlResponse(
                        error = true,
                        message = "Exception occurred: ${exception.localizedMessage}",
                        influencer_url = ""
                    )
                }
            )
        } catch (e: Exception) {
            // If the response is HTML or unexpected, log it for debugging
            println("Failed to parse response as JSON. Raw response: ${e.localizedMessage}")
            AffiliateUrlResponse(
                error = true,
                message = "Failed to parse response as JSON or unexpected response",
                influencer_url = ""
            )
        }
    }




}
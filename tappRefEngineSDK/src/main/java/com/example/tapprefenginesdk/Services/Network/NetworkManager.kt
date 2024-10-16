package com.example.wre_ref_engine_sdk.Services.Network

import com.example.wre_ref_engine_sdk.Models.Affiliate
import com.example.wre_ref_engine_sdk.Models.AffiliateUrlResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL

class NetworkManager {

    suspend fun generateAffiliateUrl(
        wreToken: String,
        influencer: String,
        adgroup: String,
        creative: String,
        mmp: Affiliate,
        token: String,
        jsonObject: Map<String, Any>
    ): AffiliateUrlResponse = withContext(Dispatchers.IO) {
        // Construct the URL for the API
        val apiUrl = "https://www.nkmhub.com/api/wre/generateUrl"

        try {
            // Create the URL object
            val url = URL(apiUrl)

            // Open a connection
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"

            // Set the headers
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Authorization", "Bearer $token")

            // Prepare the JSON body with the required parameters
            val requestBody = JSONObject().apply {
                put("wre_token", wreToken)
                put("mmp", mmp.toString())
                put("influencer", influencer)
                put("adgroup", adgroup)
                put("creative", creative)
                put("data", JSONObject(jsonObject))
            }

            // Enable output and input streams
            connection.doOutput = true
            connection.doInput = true

            // Write the request body to the output stream
            connection.outputStream.use { outputStream ->
                outputStream.write(requestBody.toString().toByteArray(Charsets.UTF_8))
            }

            // Get the response code
            val responseCode = connection.responseCode

            // Read the response
            val response = if (responseCode in 200..299) {
                // Success
                connection.inputStream.bufferedReader().use(BufferedReader::readText)
            } else {
                // Error
                connection.errorStream?.bufferedReader()?.use(BufferedReader::readText)
                    ?: "Unknown error"
            }

            // Print raw response data for debugging
            println("Raw response data: $response")

            // Parse the JSON response
            val jsonResponse = JSONObject(response)

            // Map the JSON response to AffiliateUrlResponse
            val error = jsonResponse.optBoolean("error", true)
            val message = jsonResponse.optString("message", "Unknown error")
            val affiliateUrl = jsonResponse.optString("influencer_url", "")

            AffiliateUrlResponse(
                error = error,
                message = message,
                influencer_url = affiliateUrl
            )
        } catch (e: Exception) {
            // Handle exceptions
            e.printStackTrace()
            AffiliateUrlResponse(
                error = true,
                message = "Exception occurred: ${e.localizedMessage}",
                influencer_url = ""
            )
        }
    }
}

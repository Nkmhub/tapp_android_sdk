package com.example.tapprefenginesdk.services.network

import com.example.tapprefenginesdk.models.Affiliate
import com.example.tapprefenginesdk.models.AffiliateUrlResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class NetworkManager {

    suspend fun postRequest(
        url: String,
        params: Map<String, Any>,
        headers: Map<String, String>
    ): Result<JSONObject> = withContext(Dispatchers.IO) {
        return@withContext try {
            println("Starting postRequest...")

            // Create the URL object
            val connectionUrl = URL(url)
            println("URL: $url")

            // Open a connection
            val connection = connectionUrl.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"

            // Set request headers
            headers.forEach { (key, value) ->
                connection.setRequestProperty(key, value)
            }
            println("Headers: $headers")

            connection.doOutput = true
            connection.doInput = true

            // Prepare the JSON request body
            val jsonParams = JSONObject().apply {
                params.forEach { (key, value) ->
                    if (value is Map<*, *>) {
                        put(key, JSONObject(value))
                    } else {
                        put(key, value)
                    }
                }
            }
            val requestBody = jsonParams.toString()
            println("Request Body: $requestBody")

            // Build the cURL command
            val curlCommand = buildString {
                append("curl -X POST \"$url\"")
                headers.forEach { (key, value) ->
                    append(" -H \"$key: $value\"")
                }
                append(" -d '$requestBody'")
            }
            println("Generated cURL Command: $curlCommand")

            // Write the request body directly to the output stream
            connection.outputStream.use { outputStream ->
                outputStream.write(requestBody.toByteArray(Charsets.UTF_8))
            }
            println("Request sent, waiting for response...")

            // Get the response code
            val responseCode = connection.responseCode
            println("Response Code: $responseCode")

            // Read the response
            val responseText = if (responseCode in 200..299) {
                connection.inputStream.bufferedReader().use(BufferedReader::readText)
            } else {
                connection.errorStream?.bufferedReader()?.use(BufferedReader::readText) ?: "Unknown error"
            }

            // Print raw response for debugging
            println("Raw Response Data: $responseText")

            // Parse the JSON response
            val jsonResponse = JSONObject(responseText)
            println("Parsed JSON Response: $jsonResponse")

            // Return the parsed JSON object as a successful result
            Result.success(jsonResponse)

        } catch (e: Exception) {
            println("Error in postRequest: ${e.localizedMessage}")
            e.printStackTrace()
            Result.failure(e)
        }
    }



}

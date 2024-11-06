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
            // Create the URL object
            val connectionUrl = URL(url)

            // Open a connection
            val connection = connectionUrl.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"

            // Set request headers
            headers.forEach { (key, value) ->
                connection.setRequestProperty(key, value)
            }
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            connection.doInput = true

            // Prepare the JSON request body
            val requestBody = JSONObject(params).toString()

            // Write the request body to the output stream
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(requestBody)
                writer.flush()
            }

            // Get the response code
            val responseCode = connection.responseCode

            // Read the response
            val responseText = if (responseCode in 200..299) {
                connection.inputStream.bufferedReader().use(BufferedReader::readText)
            } else {
                connection.errorStream?.bufferedReader()?.use(BufferedReader::readText) ?: "Unknown error"
            }

            // Print raw response for debugging
            println("Raw response data: $responseText")

            // Parse the JSON response
            val jsonResponse = JSONObject(responseText)

            // Return the parsed JSON object as a successful result
            Result.success(jsonResponse)

        } catch (e: Exception) {
            // Handle any exceptions and return a failure result
            e.printStackTrace()
            Result.failure(e)
        }
    }
}

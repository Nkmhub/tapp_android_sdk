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
            println("Starting postRequest...")  // Start of the method

            // Create the URL object
            val connectionUrl = URL(url)
            println("URL: $url")  // Debug the URL

            // Open a connection
            val connection = connectionUrl.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"

            // Set request headers
            headers.forEach { (key, value) ->
                connection.setRequestProperty(key, value)
            }
            println("Headers: $headers")  // Debug the headers

            connection.doOutput = true
            connection.doInput = true

            // Prepare the JSON request body
            val requestBody = JSONObject(params).toString()
            println("Request Body: $requestBody")  // Debug the request body

            // Build the cURL command
            val curlCommand = buildString {
                append("curl -X POST \"$url\"")
                headers.forEach { (key, value) ->
                    append(" -H \"$key: $value\"")
                }
                append(" -d '$requestBody'")
            }
            println("Generated cURL Command: $curlCommand")

            // Write the request body to the output stream
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(requestBody)
                writer.flush()
            }
            println("Request sent, waiting for response...")  // After sending the request

            // Get the response code
            val responseCode = connection.responseCode
            println("Response Code: $responseCode")  // Debug the response code

            // Read the response
            val responseText = if (responseCode in 200..299) {
                connection.inputStream.bufferedReader().use(BufferedReader::readText)
            } else {
                connection.errorStream?.bufferedReader()?.use(BufferedReader::readText) ?: "Unknown error"
            }

            // Print raw response for debugging
            println("Raw Response Data: $responseText")  // Debug the raw response

            // Parse the JSON response
            val jsonResponse = JSONObject(responseText)
            println("Parsed JSON Response: $jsonResponse")  // Debug the parsed JSON response

            // Return the parsed JSON object as a successful result
            Result.success(jsonResponse)

        } catch (e: Exception) {
            // Handle any exceptions and return a failure result
            println("Error in postRequest: ${e.localizedMessage}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

}

package com.example.tapp.utils

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class KeystoreUtils(context: Context) {

    private val keyAlias = "tapp_c"
    private val keyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore")
    private val sharedPreferences = context.getSharedPreferences("keystore_prefs", Context.MODE_PRIVATE)

    init {
        try {
            keyStore.load(null)
            if (!keyStore.containsAlias(keyAlias)) {
                createKey()
            }
        } catch (e: Exception) {
            Logger.logError("Keystore initialization failed: ${e.localizedMessage}")
        }
    }

    private fun createKey() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    "AndroidKeyStore"
                )
                val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                    keyAlias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setUserAuthenticationRequired(false) // Set true for biometric auth if needed
                    .build()

                keyGenerator.init(keyGenParameterSpec)
                keyGenerator.generateKey()
            }
        } catch (e: Exception) {
            Logger.logError("Failed to create key: ${e.localizedMessage}")
            throw e
        }
    }

    fun saveConfig(config: TappConfiguration) {
        Logger.logInfo("Start saving config: $config")
        val jsonConfig = Json.encodeToString(config)
        Logger.logInfo("Saving config: $jsonConfig")
        try {
            val encryptedConfig = encrypt(jsonConfig)
            Logger.logInfo("Encrypted config size: ${encryptedConfig.length}")
            sharedPreferences.edit().putString("tapp_config", encryptedConfig).apply()
        } catch (e: Exception) {
            Logger.logError("Failed to save configuration: ${e.localizedMessage}")
        }
    }

    fun getConfig(): TappConfiguration? {
        val encryptedConfig = sharedPreferences.getString("tapp_config", null)
        if (encryptedConfig == null) {
            Logger.logError("No configuration found.")
            return null
        }

        return try {
            val decryptedConfig = decrypt(encryptedConfig)
            Logger.logInfo("Retrieved config: $decryptedConfig")
            Json.decodeFromString(decryptedConfig)
        } catch (e: Exception) {
            Logger.logError("Failed to decrypt configuration: ${e.localizedMessage}")
            null
        }
    }

    fun clearConfig() {
        sharedPreferences.edit().remove("tapp_config").apply()
        Logger.logInfo("Configuration cleared.")
    }

    fun hasConfig(): Boolean {
        val hasConfig = sharedPreferences.contains("tapp_config")
        Logger.logInfo("Configuration exists: $hasConfig")
        return hasConfig
    }

    private fun encrypt(value: String): String {
        return try {
            Logger.logInfo("Starting encryption for value: ${value.take(100)}... (truncated for log)")
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            Logger.logInfo("Cipher initialized successfully")

            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
            val iv = cipher.iv
            Logger.logInfo("Generated IV: ${Base64.encodeToString(iv, Base64.DEFAULT)}")

            val encryptedData = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
            Logger.logInfo("Encrypted data length: ${encryptedData.size}")

            val ivBase64 = Base64.encodeToString(iv, Base64.DEFAULT)
            val encryptedBase64 = Base64.encodeToString(encryptedData, Base64.DEFAULT)

            "$ivBase64:$encryptedBase64".also {
                Logger.logInfo("Encryption complete. Encrypted config size: ${it.length}")
            }
        } catch (e: Exception) {
            Logger.logError("Encryption failed: ${e.localizedMessage}")
            throw IllegalStateException("Failed to encrypt value. Cause: ${e.localizedMessage}", e)
        }
    }


    private fun decrypt(encryptedValue: String): String {
        return try {
            val parts = encryptedValue.split(":")
            if (parts.size != 2) throw IllegalArgumentException("Invalid encrypted value format")

            val iv = Base64.decode(parts[0], Base64.DEFAULT)
            val encryptedData = Base64.decode(parts[1], Base64.DEFAULT)

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)
            val decryptedData = cipher.doFinal(encryptedData)
            String(decryptedData, Charsets.UTF_8)
        } catch (e: Exception) {
            Logger.logError("Decryption failed: ${e.localizedMessage}")
            throw e
        }
    }

    private fun getSecretKey(): SecretKey {
        return (keyStore.getEntry(keyAlias, null) as KeyStore.SecretKeyEntry).secretKey
    }
}


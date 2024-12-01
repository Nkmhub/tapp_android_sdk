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
        keyStore.load(null)
        if (!keyStore.containsAlias(keyAlias)) {
            createKey()
        }
    }

    // Generate a key if it doesn't already exist
    private fun createKey() {
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
    }

    // Save configuration securely
    fun saveConfig(config: TappConfiguration) {
        val jsonConfig = Json.encodeToString(config)
        val encryptedConfig = encrypt(jsonConfig)
        sharedPreferences.edit().putString("tapp_config", encryptedConfig).apply()
    }

    // Retrieve configuration securely
    fun getConfig(): TappConfiguration? {
        val encryptedConfig = sharedPreferences.getString("tapp_config", null) ?: return null
        val decryptedConfig = decrypt(encryptedConfig)
        return Json.decodeFromString(decryptedConfig)
    }

    // Clear stored configuration
    fun clearConfig() {
        sharedPreferences.edit().remove("tapp_config").apply()
    }

    // Encrypt a value
    private fun encrypt(value: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
        val iv = cipher.iv
        val encryptedData = cipher.doFinal(value.toByteArray(Charsets.UTF_8))

        // Save IV with the encrypted data (Base64 encoded)
        val ivBase64 = Base64.encodeToString(iv, Base64.DEFAULT)
        val encryptedBase64 = Base64.encodeToString(encryptedData, Base64.DEFAULT)
        return "$ivBase64:$encryptedBase64"
    }

    // Decrypt a value
    private fun decrypt(encryptedValue: String): String {
        val parts = encryptedValue.split(":")
        if (parts.size != 2) throw IllegalArgumentException("Invalid encrypted value format")

        val iv = Base64.decode(parts[0], Base64.DEFAULT)
        val encryptedData = Base64.decode(parts[1], Base64.DEFAULT)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)
        val decryptedData = cipher.doFinal(encryptedData)
        return String(decryptedData, Charsets.UTF_8)
    }

    // Retrieve the secret key from the Keystore
    private fun getSecretKey(): SecretKey {
        return (keyStore.getEntry(keyAlias, null) as KeyStore.SecretKeyEntry).secretKey
    }
}

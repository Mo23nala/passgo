package com.passgo.app.core.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MasterPasswordStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val prefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun isMasterPasswordSet(): Boolean {
        return prefs.contains(KEY_HASH) && prefs.contains(KEY_SALT)
    }

    fun saveHash(hash: ByteArray, salt: ByteArray) {
        prefs.edit()
            .putString(KEY_HASH, Base64.getEncoder().encodeToString(hash))
            .putString(KEY_SALT, Base64.getEncoder().encodeToString(salt))
            .apply()
    }

    fun loadHash(): ByteArray? {
        val hashStr = prefs.getString(KEY_HASH, null) ?: return null
        return try {
            Base64.getDecoder().decode(hashStr)
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    fun loadSalt(): ByteArray? {
        val saltStr = prefs.getString(KEY_SALT, null) ?: return null
        return try {
            Base64.getDecoder().decode(saltStr)
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "passgo_master_password"
        private const val KEY_HASH = "master_password_hash"
        private const val KEY_SALT = "master_password_salt"
    }
}

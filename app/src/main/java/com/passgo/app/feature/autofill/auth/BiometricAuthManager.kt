package com.passgo.app.feature.autofill.auth

import android.content.Context
import androidx.biometric.BiometricManager
import com.passgo.app.core.logging.PassGoLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BiometricAuthManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val logger: PassGoLogger
) {
    fun isBiometricAvailable(): BiometricAvailability {
        val authenticationTypes = BiometricManager.Authenticators.BIOMETRIC_WEAK or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL

        return try {
            val biometricManager = BiometricManager.from(context)
            when (biometricManager.canAuthenticate(authenticationTypes)) {
                BiometricManager.BIOMETRIC_SUCCESS -> BiometricAvailability.AVAILABLE
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricAvailability.NO_HARDWARE
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricAvailability.HW_UNAVAILABLE
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAvailability.NONE_ENROLLED
                BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> BiometricAvailability.SECURITY_UPDATE_REQUIRED
                BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> BiometricAvailability.UNSUPPORTED
                else -> BiometricAvailability.NOT_SUPPORTED
            }
        } catch (e: Exception) {
            logger.warn("BiometricAuthManager", "Failed to check biometric availability: ${e.message}")
            BiometricAvailability.NOT_SUPPORTED
        }
    }

    enum class BiometricAvailability {
        AVAILABLE,
        NOT_SUPPORTED,
        NO_HARDWARE,
        HW_UNAVAILABLE,
        NONE_ENROLLED,
        SECURITY_UPDATE_REQUIRED,
        UNSUPPORTED
    }
}

package com.passgo.app.feature.autofill.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.passgo.app.core.logging.PassGoLogger
import com.passgo.app.data.session.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AutofillAuthActivity : FragmentActivity() {

    @Inject lateinit var sessionManager: SessionManager
    @Inject lateinit var logger: PassGoLogger

    private var authHandled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!authHandled) {
                    authHandled = true
                    setResult(RESULT_CANCELED)
                }
                finish()
            }
        })

        showBiometricPrompt()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (!authHandled) showBiometricPrompt()
    }

    private fun showBiometricPrompt() {
        if (authHandled) return

        val executor = ContextCompat.getMainExecutor(this)

        val biometricPrompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    if (authHandled) return
                    authHandled = true
                    logger.info("AutofillAuthActivity", "Biometric authentication succeeded")
                    sessionManager.tempUnlockForAutofill()
                    setResult(RESULT_OK)
                    finish()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    if (authHandled) return
                    authHandled = true
                    logger.info("AutofillAuthActivity", "Biometric auth error: $errorCode - $errString")
                    setResult(RESULT_CANCELED, Intent().putExtra(EXTRA_AUTH_ERROR, errString.toString()))
                    finish()
                }

                override fun onAuthenticationFailed() {
                    logger.info("AutofillAuthActivity", "Biometric authentication failed (retryable)")
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(com.passgo.app.R.string.autofill_biometric_title))
            .setSubtitle(getString(com.passgo.app.R.string.autofill_biometric_subtitle))
            .setAllowedAuthenticators(
                androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK or
                    androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()

        try {
            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            logger.error("AutofillAuthActivity", "Failed to show biometric prompt: ${e.message}")
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    companion object {
        const val EXTRA_AUTH_ERROR = "extra_auth_error"
    }
}

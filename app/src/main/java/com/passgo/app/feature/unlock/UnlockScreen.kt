package com.passgo.app.feature.unlock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.passgo.app.R

@Composable
fun UnlockScreen(
    onUnlocked: () -> Unit,
    viewModel: UnlockViewModel = hiltViewModel()
) {
    val password by viewModel.password.collectAsState()
    val error by viewModel.error.collectAsState()
    val lockoutSecondsRemaining by viewModel.lockoutSecondsRemaining.collectAsState()
    val isBiometricAvailable by viewModel.isBiometricAvailable.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.isUnlocked.collect { unlocked ->
            if (unlocked) {
                onUnlocked()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.unlock_vault),
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.enter_password_continue),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = password,
            onValueChange = viewModel::onPasswordChanged,
            label = { Text(stringResource(R.string.master_password)) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            trailingIcon = {
                if (isBiometricAvailable && lockoutSecondsRemaining <= 0) {
                    IconButton(onClick = {
                        val activity = context as? FragmentActivity ?: return@IconButton
                        val executor = ContextCompat.getMainExecutor(activity)
                        val prompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
                            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                viewModel.unlockWithBiometricSuccess()
                            }
                            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                                viewModel.onBiometricError(errString.toString())
                            }
                        })
                        val promptInfo = BiometricPrompt.PromptInfo.Builder()
                            .setTitle(context.getString(R.string.autofill_biometric_title))
                            .setSubtitle(context.getString(R.string.autofill_biometric_subtitle))
                            .setAllowedAuthenticators(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK or androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                            .build()
                        prompt.authenticate(promptInfo)
                    }) {
                        Icon(Icons.Default.Fingerprint, contentDescription = context.getString(R.string.autofill_biometric_title))
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            isError = error != null
        )

        error?.let { errorText ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                errorText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = viewModel::unlock,
            enabled = password.isNotEmpty() && lockoutSecondsRemaining <= 0,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.unlock))
        }
    }
}

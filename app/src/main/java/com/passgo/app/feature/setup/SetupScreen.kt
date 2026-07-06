package com.passgo.app.feature.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.passgo.app.core.security.PasswordValidator

@Composable
fun SetupScreen(
    onComplete: () -> Unit,
    viewModel: SetupViewModel = hiltViewModel()
) {
    val password by viewModel.password.collectAsState()
    val confirmPassword by viewModel.confirmPassword.collectAsState()
    val validationResult by viewModel.validationResult.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.isComplete.collect { success ->
            if (success) onComplete()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            "Create Master Password",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "This password unlocks your vault. It cannot be recovered.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = password,
            onValueChange = viewModel::onPasswordChanged,
            label = { Text("Master Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            isError = validationResult?.isValid == false
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = viewModel::onConfirmPasswordChanged,
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            isError = confirmPassword.isNotEmpty() && !viewModel.passwordsMatch()
        )

        validationResult?.let { result ->
            Spacer(modifier = Modifier.height(16.dp))
            PasswordStrengthIndicator(result.strength)

            if (!result.isValid) {
                Spacer(modifier = Modifier.height(8.dp))
                result.errors.forEach { error ->
                    Text(
                        text = when (error) {
                            PasswordValidator.ValidationError.TOO_SHORT -> "Minimum 8 characters required"
                            PasswordValidator.ValidationError.TOO_LONG -> "Maximum 128 characters"
                            PasswordValidator.ValidationError.NO_UPPERCASE -> "Add an uppercase letter"
                            PasswordValidator.ValidationError.NO_LOWERCASE -> "Add a lowercase letter"
                            PasswordValidator.ValidationError.NO_DIGIT -> "Add a number"
                            PasswordValidator.ValidationError.NO_SPECIAL -> "Add a special character"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        if (confirmPassword.isNotEmpty() && !viewModel.passwordsMatch()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Passwords do not match",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = viewModel::createMasterPassword,
            enabled = password.isNotEmpty() && confirmPassword.isNotEmpty() && viewModel.passwordsMatch(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create Vault")
        }
    }
}

@Composable
private fun PasswordStrengthIndicator(strength: PasswordValidator.PasswordStrength) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        val color = when (strength) {
            PasswordValidator.PasswordStrength.VERY_WEAK -> MaterialTheme.colorScheme.error
            PasswordValidator.PasswordStrength.WEAK -> MaterialTheme.colorScheme.error
            PasswordValidator.PasswordStrength.MODERATE -> MaterialTheme.colorScheme.tertiary
            PasswordValidator.PasswordStrength.STRONG -> MaterialTheme.colorScheme.primary
            PasswordValidator.PasswordStrength.VERY_STRONG -> MaterialTheme.colorScheme.primary
        }
        val label = when (strength) {
            PasswordValidator.PasswordStrength.VERY_WEAK -> "Very Weak"
            PasswordValidator.PasswordStrength.WEAK -> "Weak"
            PasswordValidator.PasswordStrength.MODERATE -> "Moderate"
            PasswordValidator.PasswordStrength.STRONG -> "Strong"
            PasswordValidator.PasswordStrength.VERY_STRONG -> "Very Strong"
        }

        Text(
            text = "Strength: ",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = color
        )
    }
}

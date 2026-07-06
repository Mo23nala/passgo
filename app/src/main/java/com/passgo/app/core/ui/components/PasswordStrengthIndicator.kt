package com.passgo.app.core.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.passgo.app.core.security.PasswordValidator

@Composable
fun PasswordStrengthIndicator(
    strength: PasswordValidator.PasswordStrength,
    modifier: Modifier = Modifier
) {
    val progress = when (strength) {
        PasswordValidator.PasswordStrength.VERY_WEAK -> 0.0f
        PasswordValidator.PasswordStrength.WEAK -> 0.25f
        PasswordValidator.PasswordStrength.MODERATE -> 0.5f
        PasswordValidator.PasswordStrength.STRONG -> 0.75f
        PasswordValidator.PasswordStrength.VERY_STRONG -> 1.0f
    }

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

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Strength: ",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceContainer
        )
    }
}

@Composable
fun PasswordStrengthSuggestions(
    errors: List<PasswordValidator.ValidationError>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(top = 4.dp)) {
        errors.forEach { error ->
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

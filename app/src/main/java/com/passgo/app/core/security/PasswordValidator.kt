package com.passgo.app.core.security

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PasswordValidator @Inject constructor() {

    fun validate(password: CharArray): ValidationResult {
        val errors = mutableListOf<ValidationError>()

        if (password.size < MIN_LENGTH) {
            errors.add(ValidationError.TOO_SHORT)
        }
        if (password.size > MAX_LENGTH) {
            errors.add(ValidationError.TOO_LONG)
        }
        if (!password.any { it.isUpperCase() }) {
            errors.add(ValidationError.NO_UPPERCASE)
        }
        if (!password.any { it.isLowerCase() }) {
            errors.add(ValidationError.NO_LOWERCASE)
        }
        if (!password.any { it.isDigit() }) {
            errors.add(ValidationError.NO_DIGIT)
        }
        if (!password.any { !it.isLetterOrDigit() }) {
            errors.add(ValidationError.NO_SPECIAL)
        }

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
            strength = calculateStrength(password)
        )
    }

    fun calculateStrength(password: CharArray): PasswordStrength {
        val score = when {
            password.size >= 20 -> 5
            password.size >= 16 -> 4
            password.size >= 12 -> 3
            password.size >= 8 -> 2
            else -> 1
        }
        return when (score) {
            5 -> PasswordStrength.VERY_STRONG
            4 -> PasswordStrength.STRONG
            3 -> PasswordStrength.MODERATE
            2 -> PasswordStrength.WEAK
            else -> PasswordStrength.VERY_WEAK
        }
    }

    data class ValidationResult(
        val isValid: Boolean,
        val errors: List<ValidationError>,
        val strength: PasswordStrength
    )

    enum class ValidationError {
        TOO_SHORT,
        TOO_LONG,
        NO_UPPERCASE,
        NO_LOWERCASE,
        NO_DIGIT,
        NO_SPECIAL
    }

    enum class PasswordStrength {
        VERY_WEAK,
        WEAK,
        MODERATE,
        STRONG,
        VERY_STRONG
    }

    companion object {
        private const val MIN_LENGTH = 8
        private const val MAX_LENGTH = 128
    }
}

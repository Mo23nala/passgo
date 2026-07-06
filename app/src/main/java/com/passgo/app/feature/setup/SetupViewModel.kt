package com.passgo.app.feature.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passgo.app.core.logging.PassGoLogger
import com.passgo.app.core.security.MasterPasswordStore
import com.passgo.app.core.security.PasswordHasher
import com.passgo.app.core.security.PasswordValidator
import com.passgo.app.data.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val passwordHasher: PasswordHasher,
    private val passwordValidator: PasswordValidator,
    private val passwordStore: MasterPasswordStore,
    private val sessionManager: SessionManager,
    private val logger: PassGoLogger
) : ViewModel() {

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()

    private val _validationResult = MutableStateFlow<PasswordValidator.ValidationResult?>(null)
    val validationResult: StateFlow<PasswordValidator.ValidationResult?> = _validationResult.asStateFlow()

    private val _isComplete = MutableSharedFlow<Boolean>()
    val isComplete: SharedFlow<Boolean> = _isComplete.asSharedFlow()

    fun onPasswordChanged(value: String) {
        _password.value = value
        if (value.isNotEmpty()) {
            _validationResult.value = passwordValidator.validate(value.toCharArray())
        } else {
            _validationResult.value = null
        }
    }

    fun onConfirmPasswordChanged(value: String) {
        _confirmPassword.value = value
    }

    fun createMasterPassword() {
        val passwordChars = _password.value.toCharArray()
        val confirmChars = _confirmPassword.value.toCharArray()

        val validation = passwordValidator.validate(passwordChars)
        if (!validation.isValid) {
            _validationResult.value = validation
            passwordHasher.clearPassword(passwordChars)
            passwordHasher.clearPassword(confirmChars)
            return
        }

        if (!_password.value.contentEquals(_confirmPassword.value)) {
            passwordHasher.clearPassword(passwordChars)
            passwordHasher.clearPassword(confirmChars)
            return
        }

        viewModelScope.launch {
            try {
                val hashResult = passwordHasher.hashPassword(passwordChars)
                passwordStore.saveHash(hashResult.hash, hashResult.salt)
                logger.info("SetupViewModel", "Master password created successfully")
                sessionManager.unlock()
                _isComplete.emit(true)
            } catch (e: Exception) {
                logger.error("SetupViewModel", "Failed to create master password: ${e.message}")
                _isComplete.emit(false)
            }
        }
    }

    fun passwordsMatch(): Boolean {
        if (_confirmPassword.value.isEmpty()) return true
        return _password.value == _confirmPassword.value
    }

    override fun onCleared() {
        super.onCleared()
        _password.value.toCharArray().fill('\u0000')
        _confirmPassword.value.toCharArray().fill('\u0000')
    }
}

package com.passgo.app.feature.unlock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passgo.app.core.logging.PassGoLogger
import com.passgo.app.core.security.MasterPasswordStore
import com.passgo.app.core.security.PasswordHasher
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
class UnlockViewModel @Inject constructor(
    private val passwordHasher: PasswordHasher,
    private val passwordStore: MasterPasswordStore,
    private val sessionManager: SessionManager,
    private val logger: PassGoLogger
) : ViewModel() {

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isUnlocked = MutableSharedFlow<Boolean>()
    val isUnlocked: SharedFlow<Boolean> = _isUnlocked.asSharedFlow()

    fun onPasswordChanged(value: String) {
        _password.value = value
        _error.value = null
    }

    fun unlock() {
        val passwordChars = _password.value.toCharArray()
        if (passwordChars.isEmpty()) {
            _error.value = "Password cannot be empty"
            return
        }

        viewModelScope.launch {
            try {
                val storedHash = passwordStore.loadHash()
                val storedSalt = passwordStore.loadSalt()

                if (storedHash == null || storedSalt == null) {
                    _error.value = "Vault not initialized"
                    passwordHasher.clearPassword(passwordChars)
                    return@launch
                }

                val isValid = passwordHasher.verifyPassword(passwordChars, storedSalt, storedHash)
                if (isValid) {
                    sessionManager.unlock()
                    logger.info("UnlockViewModel", "Vault unlocked successfully")
                    _isUnlocked.emit(true)
                } else {
                    logger.info("UnlockViewModel", "Failed unlock attempt")
                    _error.value = "Invalid password"
                    _isUnlocked.emit(false)
                }
            } catch (e: Exception) {
                logger.error("UnlockViewModel", "Failed to unlock vault: ${e.message}")
                _error.value = "Unlock failed"
                passwordHasher.clearPassword(passwordChars)
                _isUnlocked.emit(false)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        _password.value.toCharArray().fill('\u0000')
    }
}

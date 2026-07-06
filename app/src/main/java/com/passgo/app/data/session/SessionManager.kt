package com.passgo.app.data.session

import com.passgo.app.core.logging.PassGoLogger
import com.passgo.app.core.security.MasterPasswordStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    private val logger: PassGoLogger,
    private val passwordStore: MasterPasswordStore
) {

    private val _sessionState = MutableStateFlow(
        if (passwordStore.isMasterPasswordSet()) SessionState.LOCKED
        else SessionState.SETUP_REQUIRED
    )
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    private var unlockedSince: Long = 0L
    private var autoLockTimeout: Long = DEFAULT_AUTO_LOCK_MS

    fun isUnlocked(): Boolean = _sessionState.value == SessionState.UNLOCKED

    fun unlock() {
        unlockedSince = System.currentTimeMillis()
        _sessionState.value = SessionState.UNLOCKED
        logger.info("SessionManager", "Session unlocked")
    }

    fun lock() {
        _sessionState.value = SessionState.LOCKED
        unlockedSince = 0L
    }

    fun setAutoLockTimeout(seconds: Int) {
        autoLockTimeout = seconds * 1000L
    }

    fun checkAndLockIfExpired(): Boolean {
        if (_sessionState.value != SessionState.UNLOCKED) return false
        val elapsed = System.currentTimeMillis() - unlockedSince
        if (elapsed >= autoLockTimeout) {
            lock()
            return true
        }
        return false
    }

    enum class SessionState {
        LOCKED,
        UNLOCKED,
        SETUP_REQUIRED
    }

    companion object {
        private const val DEFAULT_AUTO_LOCK_MS = 300_000L
    }
}

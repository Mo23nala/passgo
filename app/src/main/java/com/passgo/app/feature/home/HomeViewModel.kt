package com.passgo.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passgo.app.core.database.dao.VaultItemDao
import com.passgo.app.data.session.SessionManager
import com.passgo.app.data.settings.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val vaultItemDao: VaultItemDao,
    private val sessionManager: SessionManager,
    private val userPreferences: UserPreferences
) : ViewModel() {

    val totalItems: StateFlow<Int> = vaultItemDao.getActiveItemsCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val isUnlocked: StateFlow<Boolean> = sessionManager.sessionState
        .map { it == com.passgo.app.data.session.SessionManager.SessionState.UNLOCKED }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), sessionManager.isUnlocked())

    val securityTipsEnabled: StateFlow<Boolean> = userPreferences.securityTipsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

}

package com.passgo.app.feature.vault

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passgo.app.core.error.AppResult
import com.passgo.app.core.logging.PassGoLogger
import com.passgo.app.core.model.VaultItem
import com.passgo.app.data.repository.VaultItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ItemDetailViewModel @Inject constructor(
    private val vaultItemRepository: VaultItemRepository,
    private val logger: PassGoLogger,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _item = MutableStateFlow<VaultItem?>(null)
    val item: StateFlow<VaultItem?> = _item.asStateFlow()

    private val _passwordVisible = MutableStateFlow(false)
    val passwordVisible: StateFlow<Boolean> = _passwordVisible.asStateFlow()

    private val _copyFeedback = MutableStateFlow<String?>(null)
    val copyFeedback: StateFlow<String?> = _copyFeedback.asStateFlow()

    fun loadItem(itemId: String) {
        viewModelScope.launch {
            vaultItemRepository.getItemById(itemId).collect { loaded ->
                _item.value = loaded
            }
        }
    }

    fun togglePasswordVisibility() {
        _passwordVisible.value = !_passwordVisible.value
    }

    fun copyToClipboard(label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
        _copyFeedback.value = "$label copied"
        logger.info("ItemDetailViewModel", "Copied $label to clipboard")
    }

    fun clearCopyFeedback() {
        _copyFeedback.value = null
    }

    fun openWebsite(url: String) {
        val uri = if (url.startsWith("http://") || url.startsWith("https://")) {
            Uri.parse(url)
        } else {
            Uri.parse("https://$url")
        }
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        }
    }

    fun deleteItem(onDeleted: () -> Unit) {
        val currentId = _item.value?.id ?: return
        viewModelScope.launch {
            when (vaultItemRepository.softDelete(currentId)) {
                is AppResult.Success -> {
                    logger.info("ItemDetailViewModel", "Deleted item: $currentId")
                    onDeleted()
                }
                is AppResult.Error -> logger.error("ItemDetailViewModel", "Delete failed")
            }
        }
    }
}

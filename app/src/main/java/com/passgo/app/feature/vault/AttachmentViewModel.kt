package com.passgo.app.feature.vault

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passgo.app.core.error.AppResult
import com.passgo.app.core.logging.PassGoLogger
import com.passgo.app.core.model.Attachment
import com.passgo.app.data.repository.AttachmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AttachmentViewModel @Inject constructor(
    private val attachmentRepository: AttachmentRepository,
    private val logger: PassGoLogger
) : ViewModel() {

    private var loadJob: Job? = null
    private var currentItemId: String? = null

    private val _attachments = MutableStateFlow<List<Attachment>>(emptyList())
    val attachments: StateFlow<List<Attachment>> = _attachments.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isAdding = MutableStateFlow(false)
    val isAdding: StateFlow<Boolean> = _isAdding.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadAttachments(itemId: String) {
        if (itemId == currentItemId && loadJob?.isActive == true) return
        currentItemId = itemId
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _isLoading.value = true
            attachmentRepository.getAttachmentsForItem(itemId).collect { list ->
                _attachments.value = list
                _isLoading.value = false
            }
        }
    }

    fun addAttachment(uri: Uri, name: String, mimeType: String) {
        val itemId = currentItemId ?: return
        viewModelScope.launch {
            _isAdding.value = true
            _error.value = null
            when (val result = attachmentRepository.addAttachment(uri, itemId, name, mimeType)) {
                is AppResult.Success -> {
                    logger.info("AttachmentViewModel", "Added attachment: $name")
                }
                is AppResult.Error -> {
                    _error.value = "Failed to add attachment"
                    logger.error("AttachmentViewModel", "Failed to add attachment", result.exception)
                }
            }
            _isAdding.value = false
        }
    }

    fun deleteAttachment(id: String) {
        viewModelScope.launch {
            _error.value = null
            when (val result = attachmentRepository.deleteAttachmentPermanently(id)) {
                is AppResult.Success -> {
                    logger.info("AttachmentViewModel", "Deleted attachment: $id")
                }
                is AppResult.Error -> {
                    _error.value = "Failed to delete attachment"
                    logger.error("AttachmentViewModel", "Failed to delete attachment", result.exception)
                }
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}

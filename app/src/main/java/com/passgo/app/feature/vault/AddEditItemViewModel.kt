package com.passgo.app.feature.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passgo.app.core.error.AppResult
import com.passgo.app.core.logging.PassGoLogger
import com.passgo.app.core.model.Folder
import com.passgo.app.core.model.VaultItem
import com.passgo.app.core.model.VaultItemCategory
import com.passgo.app.core.security.PasswordGenerator
import com.passgo.app.core.security.PasswordValidator
import com.passgo.app.data.repository.FolderRepository
import com.passgo.app.data.repository.TagRepository
import com.passgo.app.data.repository.VaultItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddEditItemViewModel @Inject constructor(
    private val vaultItemRepository: VaultItemRepository,
    private val folderRepository: FolderRepository,
    private val tagRepository: TagRepository,
    private val passwordGenerator: PasswordGenerator,
    private val passwordValidator: PasswordValidator,
    private val logger: PassGoLogger
) : ViewModel() {

    private val vaultId = "default"
    private var editItemId: String? = null

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _url = MutableStateFlow("")
    val url: StateFlow<String> = _url.asStateFlow()

    private val _notes = MutableStateFlow("")
    val notes: StateFlow<String> = _notes.asStateFlow()

    private val _selectedCategory = MutableStateFlow(VaultItemCategory.OTHER)
    val selectedCategory: StateFlow<VaultItemCategory> = _selectedCategory.asStateFlow()

    private val _favorite = MutableStateFlow(false)
    val favorite: StateFlow<Boolean> = _favorite.asStateFlow()

    private val _selectedFolderId = MutableStateFlow<String?>(null)
    val selectedFolderId: StateFlow<String?> = _selectedFolderId.asStateFlow()

    private val _passwordVisible = MutableStateFlow(false)
    val passwordVisible: StateFlow<Boolean> = _passwordVisible.asStateFlow()

    private val _validationErrors = MutableStateFlow<List<String>>(emptyList())
    val validationErrors: StateFlow<List<String>> = _validationErrors.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _saveComplete = MutableStateFlow(false)
    val saveComplete: StateFlow<Boolean> = _saveComplete.asStateFlow()

    val folders: StateFlow<List<Folder>> = folderRepository.getActiveFolders(vaultId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val passwordStrength: StateFlow<PasswordValidator.PasswordStrength>
        get() = _passwordStrength
    private val _passwordStrength = MutableStateFlow(PasswordValidator.PasswordStrength.VERY_WEAK)

    fun loadItem(itemId: String) {
        editItemId = itemId
        viewModelScope.launch {
            vaultItemRepository.getItemById(itemId).collect { item ->
                if (item != null) {
                    _name.value = item.name
                    _username.value = item.username
                    _email.value = item.email
                    _password.value = item.password
                    _url.value = item.url
                    _notes.value = item.notes
                    _selectedCategory.value = item.category
                    _favorite.value = item.favorite
                    _selectedFolderId.value = item.folderId
                }
            }
        }
    }

    fun setName(value: String) { _name.value = value }
    fun setUsername(value: String) { _username.value = value }
    fun setEmail(value: String) { _email.value = value }
    fun setPassword(value: String) {
        _password.value = value
        _passwordStrength.value = passwordValidator.calculateStrength(value.toCharArray())
    }
    fun setUrl(value: String) { _url.value = value }
    fun setNotes(value: String) { _notes.value = value }
    fun setCategory(category: VaultItemCategory) { _selectedCategory.value = category }
    fun setFavorite(value: Boolean) { _favorite.value = value }
    fun setFolder(folderId: String?) { _selectedFolderId.value = folderId }
    fun togglePasswordVisibility() { _passwordVisible.value = !_passwordVisible.value }

    fun generatePassword() {
        val generated = passwordGenerator.generate(
            PasswordGenerator.GeneratorOptions(
                length = 20,
                includeUppercase = true,
                includeLowercase = true,
                includeDigits = true,
                includeSymbols = true,
                excludeAmbiguous = true
            )
        )
        _password.value = generated
        _passwordStrength.value = passwordValidator.calculateStrength(generated.toCharArray())
        logger.info("AddEditItemViewModel", "Password generated")
    }

    fun save() {
        val errors = mutableListOf<String>()
        if (_name.value.isBlank()) errors.add("Name is required")
        if (_password.value.isBlank()) errors.add("Password is required")

        _validationErrors.value = errors
        if (errors.isNotEmpty()) return

        _isSaving.value = true
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val item = VaultItem(
                id = editItemId ?: UUID.randomUUID().toString(),
                vaultId = vaultId,
                folderId = _selectedFolderId.value,
                category = _selectedCategory.value,
                name = _name.value.trim(),
                username = _username.value.trim(),
                email = _email.value.trim(),
                password = _password.value,
                url = _url.value.trim(),
                notes = _notes.value.trim(),
                favorite = _favorite.value,
                createdAt = if (editItemId != null) 0L else now,
                updatedAt = now
            )

            val result = if (editItemId != null) {
                vaultItemRepository.update(item)
            } else {
                vaultItemRepository.insert(item)
            }

            when (result) {
                is AppResult.Success -> {
                    logger.info("AddEditItemViewModel", "Saved item: ${item.id}")
                    _saveComplete.value = true
                }
                is AppResult.Error -> {
                    logger.error("AddEditItemViewModel", "Save failed")
                    _validationErrors.value = listOf("Failed to save item")
                }
            }
            _isSaving.value = false
        }
    }
}

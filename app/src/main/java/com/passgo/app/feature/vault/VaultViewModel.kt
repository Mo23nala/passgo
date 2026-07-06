package com.passgo.app.feature.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passgo.app.core.error.AppResult
import com.passgo.app.core.logging.PassGoLogger
import com.passgo.app.core.model.Folder
import com.passgo.app.core.model.VaultItem
import com.passgo.app.core.model.VaultItemCategory
import com.passgo.app.data.repository.FolderRepository
import com.passgo.app.data.repository.VaultItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

enum class SortOption { RECENT, NAME, FAVORITE }

@HiltViewModel
class VaultViewModel @Inject constructor(
    private val vaultItemRepository: VaultItemRepository,
    private val folderRepository: FolderRepository,
    private val logger: PassGoLogger
) : ViewModel() {

    private val vaultId = "default"

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.RECENT)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    private val _selectedCategory = MutableStateFlow<VaultItemCategory?>(null)
    val selectedCategory: StateFlow<VaultItemCategory?> = _selectedCategory.asStateFlow()

    private val _favoritesOnly = MutableStateFlow(false)
    val favoritesOnly: StateFlow<Boolean> = _favoritesOnly.asStateFlow()

    private val _selectedFolderId = MutableStateFlow<String?>(null)
    val selectedFolderId: StateFlow<String?> = _selectedFolderId.asStateFlow()

    val folders: StateFlow<List<Folder>> = folderRepository.getActiveFolders(vaultId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val items: StateFlow<List<VaultItem>> = combine(
        _searchQuery, _selectedCategory, _favoritesOnly, _selectedFolderId, _sortOption
    ) { query, category, favorites, folderId, sort ->
        FilterState(query, category, favorites, folderId, sort)
    }.flatMapLatest { state ->
        val flow = when {
            state.query.isNotBlank() && state.category != null ->
                vaultItemRepository.searchByType(vaultId, state.category, state.query)
            state.query.isNotBlank() && state.favorites ->
                vaultItemRepository.searchFavorites(vaultId, state.query)
            state.query.isNotBlank() && state.folderId != null ->
                vaultItemRepository.searchByFolder(vaultId, state.folderId, state.query)
            state.query.isNotBlank() ->
                vaultItemRepository.searchItems(vaultId, state.query)
            state.category != null ->
                vaultItemRepository.getByType(vaultId, state.category)
            state.favorites ->
                vaultItemRepository.getFavorites(vaultId)
            state.folderId != null ->
                vaultItemRepository.getByFolder(state.folderId)
            state.sort == SortOption.NAME ->
                vaultItemRepository.getActiveItemsSortedByName(vaultId)
            state.sort == SortOption.FAVORITE ->
                vaultItemRepository.getActiveItemsSortedByFavorite(vaultId)
            else ->
                vaultItemRepository.getActiveItems(vaultId)
        }
        flow
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setSortOption(option: SortOption) { _sortOption.value = option }
    fun setCategory(category: VaultItemCategory?) { _selectedCategory.value = category }
    fun setFavoritesOnly(favorites: Boolean) { _favoritesOnly.value = favorites }
    fun setFolderFilter(folderId: String?) { _selectedFolderId.value = folderId }

    fun clearFilters() {
        _selectedCategory.value = null
        _favoritesOnly.value = false
        _selectedFolderId.value = null
        _searchQuery.value = ""
        _sortOption.value = SortOption.RECENT
    }

    fun deleteItem(id: String) {
        viewModelScope.launch {
            when (vaultItemRepository.softDelete(id)) {
                is AppResult.Success -> logger.info("VaultViewModel", "Deleted vault item: $id")
                is AppResult.Error -> logger.error("VaultViewModel", "Delete failed")
            }
        }
    }

    fun toggleFavorite(item: VaultItem) {
        viewModelScope.launch {
            when (vaultItemRepository.update(item.copy(favorite = !item.favorite))) {
                is AppResult.Success -> { /* success */ }
                is AppResult.Error -> logger.error("VaultViewModel", "Toggle favorite failed")
            }
        }
    }

    private data class FilterState(
        val query: String,
        val category: VaultItemCategory?,
        val favorites: Boolean,
        val folderId: String?,
        val sort: SortOption
    )
}

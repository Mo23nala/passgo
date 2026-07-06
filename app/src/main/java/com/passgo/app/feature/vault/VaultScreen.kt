package com.passgo.app.feature.vault

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.passgo.app.core.model.VaultItem
import com.passgo.app.core.model.VaultItemCategory

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VaultScreen(
    onAddItem: () -> Unit,
    onItemClick: (String) -> Unit,
    viewModel: VaultViewModel = hiltViewModel()
) {
    val items by viewModel.items.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val favoritesOnly by viewModel.favoritesOnly.collectAsState()
    val selectedFolderId by viewModel.selectedFolderId.collectAsState()
    val folders by viewModel.folders.collectAsState()

    var showSortMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::setSearchQuery,
                placeholder = { Text("Search items") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Sort: ${sortOption.name.lowercase().replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { showSortMenu = true }
                )
                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false }
                ) {
                    SortOption.entries.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            onClick = {
                                viewModel.setSortOption(option)
                                showSortMenu = false
                            }
                        )
                    }
                }

                if (selectedCategory != null || favoritesOnly || selectedFolderId != null) {
                    TextButton(onClick = viewModel::clearFilters) {
                        Text("Clear Filters")
                    }
                }
            }

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VaultItemCategory.entries.take(6).forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = {
                            viewModel.setCategory(if (selectedCategory == category) null else category)
                        },
                        label = { Text(category.displayName) }
                    )
                }
            }

            if (folders.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    folders.take(4).forEach { folder ->
                        FilterChip(
                            selected = selectedFolderId == folder.id,
                            onClick = {
                                viewModel.setFolderFilter(if (selectedFolderId == folder.id) null else folder.id)
                            },
                            label = { Text(folder.name) }
                        )
                    }
                }
            }

            if (items.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            if (searchQuery.isNotEmpty() || selectedCategory != null || favoritesOnly || selectedFolderId != null)
                                "No matching items"
                            else
                                "No items yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            if (searchQuery.isEmpty() && selectedCategory == null && !favoritesOnly && selectedFolderId == null)
                                "Tap + to add your first item"
                            else
                                "Try adjusting your search or filters",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items, key = { it.id }) { item ->
                        ItemCard(
                            item = item,
                            onClick = { onItemClick(item.id) },
                            onToggleFavorite = { viewModel.toggleFavorite(item) },
                            onDelete = { showDeleteDialog = item.id }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }

        FloatingActionButton(
            onClick = onAddItem,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add item")
        }
    }

    showDeleteDialog?.let { itemId ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Item") },
            text = { Text("Move this item to trash?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteItem(itemId)
                    showDeleteDialog = null
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ItemCard(
    item: VaultItem,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryIcon(category = item.category)

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (item.username.isNotEmpty()) {
                    Text(
                        item.username,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        item.category.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (item.favorite) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            IconButton(onClick = onToggleFavorite) {
                Icon(
                    if (item.favorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                    contentDescription = if (item.favorite) "Unfavorite" else "Favorite",
                    tint = if (item.favorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CategoryIcon(category: VaultItemCategory) {
    val icon = when (category) {
        VaultItemCategory.GOOGLE_ACCOUNT, VaultItemCategory.EMAIL -> Icons.Default.Folder
        VaultItemCategory.SOCIAL_MEDIA, VaultItemCategory.ENTERTAINMENT,
        VaultItemCategory.GAMING -> Icons.Default.Star
        VaultItemCategory.BANKING, VaultItemCategory.SHOPPING,
        VaultItemCategory.WORK -> Icons.Default.Folder
        VaultItemCategory.WIFI -> Icons.Default.Folder
        VaultItemCategory.SOFTWARE_LICENSE -> Icons.Default.Folder
        VaultItemCategory.SECURE_NOTE -> Icons.Default.Folder
        VaultItemCategory.OTHER -> Icons.Outlined.Folder
    }
    Icon(
        icon,
        contentDescription = null,
        modifier = Modifier.size(32.dp),
        tint = MaterialTheme.colorScheme.primary
    )
}

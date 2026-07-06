package com.passgo.app.feature.vault

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import kotlinx.coroutines.delay

@Composable
fun ItemDetailScreen(
    itemId: String,
    onNavigateBack: () -> Unit,
    onEdit: (String) -> Unit,
    viewModel: ItemDetailViewModel = hiltViewModel()
) {
    val item by viewModel.item.collectAsState()
    val passwordVisible by viewModel.passwordVisible.collectAsState()
    val copyFeedback by viewModel.copyFeedback.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(itemId) {
        viewModel.loadItem(itemId)
    }

    LaunchedEffect(copyFeedback) {
        if (copyFeedback != null) {
            delay(2000)
            viewModel.clearCopyFeedback()
        }
    }

    val vaultItem = item

    if (vaultItem == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = { onEdit(vaultItem.id) }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }

        Text(
            vaultItem.name,
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            vaultItem.category.displayName,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (vaultItem.username.isNotEmpty()) {
            DetailField(
                label = "Username",
                value = vaultItem.username,
                onCopy = { viewModel.copyToClipboard("Username", vaultItem.username) }
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        }

        if (vaultItem.email.isNotEmpty()) {
            DetailField(
                label = "Email",
                value = vaultItem.email,
                onCopy = { viewModel.copyToClipboard("Email", vaultItem.email) }
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        }

        DetailField(
            label = "Password",
            value = if (passwordVisible) vaultItem.password else "\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022",
            onCopy = { viewModel.copyToClipboard("Password", vaultItem.password) },
            trailing = {
                IconButton(onClick = viewModel::togglePasswordVisibility) {
                    Icon(
                        if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) "Hide" else "Show"
                    )
                }
            }
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        if (vaultItem.url.isNotEmpty()) {
            DetailField(
                label = "Website",
                value = vaultItem.url,
                onCopy = { viewModel.copyToClipboard("Website", vaultItem.url) },
                trailing = {
                    IconButton(onClick = { viewModel.openWebsite(vaultItem.url) }) {
                        Icon(Icons.Default.Language, contentDescription = "Open")
                    }
                }
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        }

        if (vaultItem.notes.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Notes",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        vaultItem.notes,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        if (vaultItem.favorite) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "\u2605 Favorite",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Item") },
                text = { Text("Move this item to trash?") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteItem(onNavigateBack)
                        showDeleteDialog = false
                    }) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        copyFeedback?.let { feedback ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                action = {
                    TextButton(onClick = { viewModel.clearCopyFeedback() }) {
                        Text("Dismiss")
                    }
                }
            ) {
                Text(feedback)
            }
        }
    }
}

@Composable
private fun DetailField(
    label: String,
    value: String,
    onCopy: () -> Unit,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                value,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(onClick = onCopy) {
            Icon(Icons.Default.ContentCopy, contentDescription = "Copy $label")
        }
        trailing?.invoke()
    }
}

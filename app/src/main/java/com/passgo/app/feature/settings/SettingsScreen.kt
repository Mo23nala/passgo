package com.passgo.app.feature.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.passgo.app.R
import com.passgo.app.data.settings.ThemeMode
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val autoLockSeconds by viewModel.autoLockSeconds.collectAsState()
    val clipboardClearEnabled by viewModel.clipboardClearEnabled.collectAsState()
    val clipboardClearDelayMs by viewModel.clipboardClearDelayMs.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/x-sqlite3")) { uri ->
        uri?.let {
            viewModel.exportDatabase(it) { success, errorMsg ->
                scope.launch {
                    val msg = if (success) context.getString(R.string.backup_success) else context.getString(R.string.backup_failed, errorMsg)
                    snackbarHostState.showSnackbar(msg)
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            viewModel.importDatabase(it) { success, errorMsg ->
                scope.launch {
                    val msg = if (success) context.getString(R.string.restore_success) else context.getString(R.string.restore_failed, errorMsg)
                    snackbarHostState.showSnackbar(msg)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Settings",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Theme Section
            SettingsSectionTitle(Icons.Default.DarkMode, "Appearance")
            ThemeSelection(currentTheme = themeMode, onThemeSelected = viewModel::setThemeMode)

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // Security Section
            SettingsSectionTitle(Icons.Default.Lock, "Security")

            SettingsItem(
                title = "Auto-lock Timeout",
                subtitle = "Require master password after inactivity"
            )
            AutoLockSelection(
                currentSeconds = autoLockSeconds,
                onSecondsSelected = viewModel::setAutoLockSeconds
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.setClipboardClearEnabled(!clipboardClearEnabled) }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Clear Clipboard Automatically", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "Remove copied passwords from clipboard after a delay",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = clipboardClearEnabled,
                    onCheckedChange = viewModel::setClipboardClearEnabled
                )
            }

            if (clipboardClearEnabled) {
                ClipboardDelaySelection(
                    currentDelayMs = clipboardClearDelayMs,
                    onDelaySelected = viewModel::setClipboardClearDelayMs
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // Data Management Section
            SettingsSectionTitle(Icons.Default.Backup, stringResource(R.string.data_management))
            Text(
                stringResource(R.string.backup_warning),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            SettingsItem(
                title = stringResource(R.string.export_backup),
                onClick = { exportLauncher.launch("passgo_backup.db") }
            )
            SettingsItem(
                title = stringResource(R.string.import_backup),
                onClick = { importLauncher.launch(arrayOf("application/octet-stream", "application/x-sqlite3", "*/*")) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // About Section
            SettingsSectionTitle(Icons.Default.Info, "About")
            SettingsItem(
                title = "Version",
                subtitle = viewModel.appVersion
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingsSectionTitle(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(end = 12.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ThemeSelection(currentTheme: ThemeMode, onThemeSelected: (ThemeMode) -> Unit) {
    Column {
        ThemeOption(
            title = "System Default",
            selected = currentTheme == ThemeMode.SYSTEM,
            onClick = { onThemeSelected(ThemeMode.SYSTEM) }
        )
        ThemeOption(
            title = "Light",
            selected = currentTheme == ThemeMode.LIGHT,
            onClick = { onThemeSelected(ThemeMode.LIGHT) }
        )
        ThemeOption(
            title = "Dark",
            selected = currentTheme == ThemeMode.DARK,
            onClick = { onThemeSelected(ThemeMode.DARK) }
        )
    }
}

@Composable
private fun ThemeOption(title: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            modifier = Modifier.padding(end = 12.dp)
        )
        Text(title, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun AutoLockSelection(currentSeconds: Int, onSecondsSelected: (Int) -> Unit) {
    val options = listOf(
        30 to "30 seconds",
        60 to "1 minute",
        300 to "5 minutes",
        900 to "15 minutes",
        1800 to "30 minutes"
    )

    Column {
        options.forEach { (seconds, label) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSecondsSelected(seconds) }
                    .padding(vertical = 8.dp, horizontal = 16.dp)
            ) {
                RadioButton(
                    selected = currentSeconds == seconds,
                    onClick = { onSecondsSelected(seconds) },
                    modifier = Modifier.padding(end = 12.dp)
                )
                Text(label, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun ClipboardDelaySelection(currentDelayMs: Long, onDelaySelected: (Long) -> Unit) {
    val options = listOf(
        10000L to "10 seconds",
        30000L to "30 seconds",
        60000L to "1 minute",
        120000L to "2 minutes"
    )

    Column(modifier = Modifier.padding(start = 16.dp)) {
        options.forEach { (delayMs, label) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDelaySelected(delayMs) }
                    .padding(vertical = 4.dp, horizontal = 16.dp)
            ) {
                RadioButton(
                    selected = currentDelayMs == delayMs,
                    onClick = { onDelaySelected(delayMs) },
                    modifier = Modifier.padding(end = 12.dp)
                )
                Text(label, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 12.dp)
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        if (subtitle != null) {
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

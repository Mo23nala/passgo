package com.passgo.app.feature.vault

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachmentPreviewScreen(
    attachmentId: String,
    onNavigateBack: () -> Unit,
    viewModel: AttachmentPreviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(attachmentId) {
        viewModel.loadAttachment(attachmentId)
    }

    val title = when (val state = uiState) {
        is PreviewUiState.Loading -> "Preview"
        is PreviewUiState.ImageContent -> state.attachment.name
        is PreviewUiState.TextContent -> state.attachment.name
        is PreviewUiState.ExternalContent -> state.attachment.name
        is PreviewUiState.Error -> "Preview"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is PreviewUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is PreviewUiState.ImageContent -> {
                    ImagePreviewContent(state.uri)
                }

                is PreviewUiState.TextContent -> {
                    TextPreviewContent(state.content)
                }

                is PreviewUiState.ExternalContent -> {
                    LaunchedEffect(state.uri) {
                        viewModel.launchExternalViewer(context)
                    }
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Opening with external app...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                is PreviewUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Tap back to return",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ImagePreviewContent(uri: android.net.Uri) {
    val context = LocalContext.current
    var imageBitmap by remember { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }

    LaunchedEffect(uri) {
        withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val bitmap = BitmapFactory.decodeStream(stream)
                if (bitmap != null) {
                    val bmp = bitmap.asImageBitmap()
                    withContext(Dispatchers.Main) {
                        imageBitmap = bmp
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        imageBitmap?.let {
            Image(
                bitmap = it,
                contentDescription = "Preview",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentScale = ContentScale.Fit
            )
        } ?: CircularProgressIndicator()
    }
}

@Composable
private fun TextPreviewContent(content: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

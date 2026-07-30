package com.denxhinjo.fabinventory.ui.admin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.denxhinjo.fabinventory.data.remote.dto.LocationResponse
import com.denxhinjo.fabinventory.ui.common.AppCard
import com.denxhinjo.fabinventory.ui.common.FullScreenError
import com.denxhinjo.fabinventory.ui.common.FullScreenLoading

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUserAccessScreen(
    onDone: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditUserAccessViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.saved) {
        if (uiState.saved) onDone()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(uiState.userName.ifBlank { "Manage access" }) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cancel")
                    }
                },
            )
        },
    ) { padding ->
        when {
            uiState.isLoading -> FullScreenLoading(modifier = Modifier.padding(padding))
            uiState.error != null && uiState.allLocations.isEmpty() -> FullScreenError(
                message = uiState.error ?: "Something went wrong",
                onRetry = viewModel::load,
                modifier = Modifier.padding(padding),
            )
            else -> Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                Text(
                    "Warehouses this user can add or edit products in:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp),
                )
                LazyColumn(
                    modifier = Modifier
                        .weight(1f, fill = true)
                        .fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                ) {
                    items(uiState.allLocations, key = { it.id }) { location ->
                        LocationCheckRow(
                            location = location,
                            checked = location.id in uiState.selectedLocationIds,
                            onToggle = { viewModel.toggleLocation(location.id) },
                        )
                    }
                }

                uiState.error?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }

                Button(
                    onClick = viewModel::save,
                    enabled = !uiState.isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.padding(4.dp))
                    } else {
                        Text("Save access")
                    }
                }
            }
        }
    }
}

@Composable
private fun LocationCheckRow(location: LocationResponse, checked: Boolean, onToggle: () -> Unit) {
    AppCard(
        onClick = onToggle,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = checked, onCheckedChange = { onToggle() })
            Column {
                Text(location.name, style = MaterialTheme.typography.titleMedium)
                location.city?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            }
        }
    }
}

package com.denxhinjo.fabinventory.ui.admin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.denxhinjo.fabinventory.data.remote.dto.UserResponse
import com.denxhinjo.fabinventory.ui.common.AppCard
import com.denxhinjo.fabinventory.ui.common.FullScreenError
import com.denxhinjo.fabinventory.ui.common.FullScreenLoading

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageAccessScreen(
    onBack: () -> Unit,
    onUserClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ManageAccessViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Manage warehouse access") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        when {
            uiState.isLoading -> FullScreenLoading(modifier = Modifier.padding(padding))
            uiState.error != null -> FullScreenError(
                message = uiState.error ?: "Something went wrong",
                onRetry = viewModel::load,
                modifier = Modifier.padding(padding),
            )
            uiState.users.isEmpty() -> Column(modifier = Modifier.padding(padding).padding(24.dp)) {
                Text("No non-admin users found.", style = MaterialTheme.typography.bodyMedium)
            }
            else -> LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
            ) {
                items(uiState.users, key = { it.id }) { user ->
                    UserRow(user = user, onClick = { onUserClick(user.id) })
                }
            }
        }
    }
}

@Composable
private fun UserRow(user: UserResponse, onClick: () -> Unit) {
    AppCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(user.fullName, style = MaterialTheme.typography.titleMedium)
            Text("${user.email} · ${user.username}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

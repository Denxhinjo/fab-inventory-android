package com.denxhinjo.fabinventory.ui.users

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
fun UsersListScreen(
    onBack: () -> Unit,
    onAddClick: () -> Unit,
    onUserClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: UsersListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var pendingDeleteId by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Users") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Filled.Add, contentDescription = "Add user")
            }
        },
    ) { padding ->
        when {
            uiState.isLoading -> FullScreenLoading(modifier = Modifier.padding(padding))
            uiState.error != null -> FullScreenError(
                message = uiState.error ?: "Something went wrong",
                onRetry = viewModel::load,
                modifier = Modifier.padding(padding),
            )
            else -> Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                uiState.deleteError?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                ) {
                    items(uiState.users, key = { it.id }) { user ->
                        UserRow(
                            user = user,
                            onClick = { onUserClick(user.id) },
                            onDeleteClick = { pendingDeleteId = user.id },
                        )
                    }
                }
            }
        }
    }

    pendingDeleteId?.let { id ->
        val user = uiState.users.find { it.id == id }
        AlertDialog(
            onDismissRequest = { pendingDeleteId = null },
            title = { Text("Delete ${user?.fullName ?: "this user"}?") },
            text = { Text("This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    pendingDeleteId = null
                    viewModel.deleteUser(id)
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteId = null }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun UserRow(user: UserResponse, onClick: () -> Unit, onDeleteClick: () -> Unit) {
    AppCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(user.fullName, style = MaterialTheme.typography.titleMedium)
                Text(
                    buildString {
                        append(user.email)
                        append(" · ${user.role.replaceFirstChar { it.uppercase() }}")
                        if (!user.isActive) append(" · Inactive")
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (!user.isActive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete user")
            }
        }
    }
}

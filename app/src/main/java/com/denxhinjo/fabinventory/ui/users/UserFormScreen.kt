package com.denxhinjo.fabinventory.ui.users

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.denxhinjo.fabinventory.ui.common.FullScreenLoading

private val roleOptions = listOf("user", "admin")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserFormScreen(
    onDone: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: UserFormViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.saved) {
        if (uiState.saved) onDone()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditMode) "Edit user" else "New user") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cancel")
                    }
                },
            )
        },
    ) { padding ->
        if (uiState.isLoading) {
            FullScreenLoading(modifier = Modifier.padding(padding))
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            OutlinedTextField(
                value = uiState.fullName,
                onValueChange = viewModel::onFullNameChange,
                label = { Text("Full name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChange,
                label = { Text("Email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            )
            if (!uiState.isEditMode) {
                OutlinedTextField(
                    value = uiState.username,
                    onValueChange = viewModel::onUsernameChange,
                    label = { Text("Username") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                )
            }
            OutlinedTextField(
                value = uiState.phone,
                onValueChange = viewModel::onPhoneChange,
                label = { Text("Phone (optional)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            )
            OutlinedTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                label = { Text(if (uiState.isEditMode) "New password (optional)" else "Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            )

            Text("Role", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 16.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                roleOptions.forEachIndexed { index, role ->
                    SegmentedButton(
                        selected = uiState.role == role,
                        onClick = { viewModel.onRoleChange(role) },
                        shape = androidx.compose.material3.SegmentedButtonDefaults.itemShape(index, roleOptions.size),
                    ) {
                        Text(role.replaceFirstChar { it.uppercase() })
                    }
                }
            }

            if (uiState.isEditMode) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Active", style = MaterialTheme.typography.labelLarge)
                        Text(
                            "Inactive users can't log in",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(checked = uiState.isActive, onCheckedChange = viewModel::onActiveChange)
                }
            }

            uiState.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }

            Button(
                onClick = viewModel::submit,
                enabled = !uiState.isSaving,
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.padding(4.dp))
                } else {
                    Text(if (uiState.isEditMode) "Save changes" else "Create user")
                }
            }
        }
    }
}

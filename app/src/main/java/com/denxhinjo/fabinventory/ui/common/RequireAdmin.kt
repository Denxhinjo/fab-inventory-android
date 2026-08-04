package com.denxhinjo.fabinventory.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.denxhinjo.fabinventory.R
import com.denxhinjo.fabinventory.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SessionGuardViewModel @Inject constructor(
    authRepository: AuthRepository,
) : ViewModel() {
    // null while the session is still loading, so the guard shows a spinner
    // instead of flashing "access denied" before we actually know the role.
    val isAdmin: StateFlow<Boolean?> = authRepository.sessionFlow
        .map { session -> session?.let { it.role == "admin" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}

/**
 * Wraps admin-only screens so reaching one by any means other than the
 * (already role-gated) nav UI -- a restored back stack, a deep link, a
 * stale bookmark -- shows a real access-denied screen instead of relying
 * only on the backend 403ing whatever API call the screen happens to make.
 */
@Composable
fun RequireAdmin(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SessionGuardViewModel = hiltViewModel(),
    content: @Composable () -> Unit,
) {
    val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycle()
    when (isAdmin) {
        null -> FullScreenLoading(modifier = modifier)
        false -> AccessDeniedScreen(onBack = onBack, modifier = modifier)
        true -> content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessDeniedScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.access_denied_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                Icons.Filled.Lock,
                contentDescription = null,
                modifier = Modifier,
                tint = MaterialTheme.colorScheme.error,
            )
            Text(
                stringResource(R.string.access_denied_heading),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp),
            )
            Text(
                stringResource(R.string.access_denied_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
            Button(onClick = onBack, modifier = Modifier.padding(top = 24.dp)) {
                Text(stringResource(R.string.access_denied_go_back))
            }
        }
    }
}

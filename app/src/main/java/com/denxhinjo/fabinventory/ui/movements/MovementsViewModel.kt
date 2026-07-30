package com.denxhinjo.fabinventory.ui.movements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denxhinjo.fabinventory.data.remote.dto.StockMovementResponse
import com.denxhinjo.fabinventory.data.repository.StockMovementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MovementsUiState(
    val movements: List<StockMovementResponse> = emptyList(),
    val page: Int = 1,
    val hasMore: Boolean = true,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class MovementsViewModel @Inject constructor(
    private val stockMovementRepository: StockMovementRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MovementsUiState())
    val uiState: StateFlow<MovementsUiState> = _uiState.asStateFlow()

    init {
        load(reset = true)
    }

    fun refresh() = load(reset = true)

    fun loadMore() {
        if (_uiState.value.hasMore && !_uiState.value.isLoadingMore) {
            load(reset = false)
        }
    }

    private fun load(reset: Boolean) {
        val current = _uiState.value
        val page = if (reset) 1 else current.page + 1

        viewModelScope.launch {
            _uiState.update {
                if (reset) it.copy(isLoading = true, error = null) else it.copy(isLoadingMore = true)
            }
            val result = stockMovementRepository.getMovements(page = page)
            result.fold(
                onSuccess = { response ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            movements = if (reset) response.items else it.movements + response.items,
                            page = response.page,
                            hasMore = response.page < response.totalPages,
                            error = null,
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(isLoading = false, isLoadingMore = false, error = e.message ?: "Failed to load movements")
                    }
                },
            )
        }
    }
}

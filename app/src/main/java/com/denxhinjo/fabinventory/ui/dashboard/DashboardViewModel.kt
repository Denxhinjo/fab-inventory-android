package com.denxhinjo.fabinventory.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denxhinjo.fabinventory.data.local.UserSession
import com.denxhinjo.fabinventory.data.remote.dto.CategoryResponse
import com.denxhinjo.fabinventory.data.remote.dto.DashboardResponse
import com.denxhinjo.fabinventory.data.remote.dto.SupplierResponse
import com.denxhinjo.fabinventory.data.repository.AuthRepository
import com.denxhinjo.fabinventory.data.repository.DashboardRepository
import com.denxhinjo.fabinventory.data.repository.ProductRepository
import com.denxhinjo.fabinventory.data.repository.StockMovementRepository
import com.denxhinjo.fabinventory.data.repository.SupplierRepository
import com.denxhinjo.fabinventory.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository,
    private val productRepository: ProductRepository,
    private val stockMovementRepository: StockMovementRepository,
    private val supplierRepository: SupplierRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<DashboardResponse>>(UiState.Loading)
    val uiState: StateFlow<UiState<DashboardResponse>> = _uiState.asStateFlow()

    private val _categories = MutableStateFlow<List<CategoryResponse>>(emptyList())
    val categories: StateFlow<List<CategoryResponse>> = _categories.asStateFlow()

    private val _trend = MutableStateFlow<List<DayPoint>>(emptyList())
    val trend: StateFlow<List<DayPoint>> = _trend.asStateFlow()

    private val _topSuppliers = MutableStateFlow<List<SupplierResponse>>(emptyList())
    val topSuppliers: StateFlow<List<SupplierResponse>> = _topSuppliers.asStateFlow()

    val session: StateFlow<UserSession?> = authRepository.sessionFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = dashboardRepository.getDashboard()
            _uiState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Failed to load dashboard") },
            )
        }
        viewModelScope.launch {
            productRepository.getCategoriesFull().onSuccess { _categories.value = it }
        }
        viewModelScope.launch {
            loadTrend()
        }
        viewModelScope.launch {
            supplierRepository.getAllSuppliers().onSuccess { suppliers ->
                _topSuppliers.value = suppliers
                    .sortedByDescending { it.productCount ?: 0 }
                    .filter { (it.productCount ?: 0) > 0 }
                    .take(3)
            }
        }
    }

    private suspend fun loadTrend() {
        val today = LocalDate.now()
        val from = today.minusDays(13)
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        stockMovementRepository.getMovementsInRange(from.format(formatter), today.format(formatter))
            .onSuccess { response ->
                val byDate = response.items.groupBy { it.movementDate }
                val days = (0..13).map { offset -> from.plusDays(offset.toLong()) }
                _trend.value = days.map { day ->
                    val dayKey = day.format(formatter)
                    val dayMovements = byDate[dayKey].orEmpty()
                    DayPoint(
                        label = day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                        stockIn = dayMovements.filter { it.movementType == "Stock In" }.sumOf { it.quantity },
                        stockOut = dayMovements.filter { it.movementType == "Stock Out" }.sumOf { it.quantity },
                    )
                }
            }
    }

    fun logout() {
        viewModelScope.launch { authRepository.logout() }
    }
}

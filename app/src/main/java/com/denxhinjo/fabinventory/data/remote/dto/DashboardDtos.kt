package com.denxhinjo.fabinventory.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Mirrors the response shape built by backend/app/routers/dashboard.py::get_dashboard_stats.
 */
@Serializable
data class DashboardResponse(
    val stats: DashboardStats,
    @SerialName("stock_summary") val stockSummary: StockSummary,
    @SerialName("work_process_by_status") val workProcessByStatus: Map<String, Int>,
    @SerialName("recent_activity") val recentActivity: List<RecentActivityItem>,
    @SerialName("low_stock_items") val lowStockItems: List<LowStockItem>,
)

@Serializable
data class DashboardStats(
    @SerialName("total_products") val totalProducts: Int,
    @SerialName("low_stock_products") val lowStockProducts: Int,
    @SerialName("total_locations") val totalLocations: Int,
    @SerialName("active_work_processes") val activeWorkProcesses: Int,
    @SerialName("completed_work_processes") val completedWorkProcesses: Int,
    @SerialName("total_users") val totalUsers: Int,
)

@Serializable
data class StockSummary(
    @SerialName("stock_in_30d") val stockIn30d: Double,
    @SerialName("stock_out_30d") val stockOut30d: Double,
)

@Serializable
data class RecentActivityItem(
    val id: Int,
    val type: String,
    @SerialName("product_name") val productName: String,
    val quantity: Double,
    val unit: String,
    @SerialName("user_name") val userName: String,
    val date: String,
    @SerialName("created_at") val createdAt: String,
)

@Serializable
data class LowStockItem(
    val id: Int,
    val name: String,
    val quantity: Double,
    @SerialName("min_stock_level") val minStockLevel: Double,
    val unit: String,
    val location: String? = null,
)

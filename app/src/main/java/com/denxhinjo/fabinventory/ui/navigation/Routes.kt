package com.denxhinjo.fabinventory.ui.navigation

object Routes {
    const val ARG_PRODUCT_ID = "productId"
    const val ARG_PREFILLED_PRODUCT_ID = "prefilledProductId"

    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val PRODUCTS = "products"
    const val MOVEMENTS = "movements"

    const val PRODUCT_DETAIL = "product_detail/{$ARG_PRODUCT_ID}"
    const val CREATE_MOVEMENT = "create_movement?$ARG_PREFILLED_PRODUCT_ID={$ARG_PREFILLED_PRODUCT_ID}"

    fun productDetail(id: Int) = "product_detail/$id"

    fun createMovement(productId: Int? = null): String =
        if (productId != null) "create_movement?$ARG_PREFILLED_PRODUCT_ID=$productId" else "create_movement"
}

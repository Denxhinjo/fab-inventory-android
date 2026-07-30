package com.denxhinjo.fabinventory.ui.navigation

object Routes {
    const val ARG_PRODUCT_ID = "productId"
    const val ARG_PREFILLED_PRODUCT_ID = "prefilledProductId"
    const val ARG_EDIT_PRODUCT_ID = "editProductId"
    const val ARG_USER_ID = "userId"
    const val ARG_EDIT_LOCATION_ID = "editLocationId"
    const val ARG_EDIT_SUPPLIER_ID = "editSupplierId"

    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val PRODUCTS = "products"
    const val MOVEMENTS = "movements"
    const val ADMIN_HOME = "admin_home"
    const val MANAGE_ACCESS = "manage_access"
    const val LOCATIONS_LIST = "locations_list"
    const val SUPPLIERS_LIST = "suppliers_list"

    const val PRODUCT_DETAIL = "product_detail/{$ARG_PRODUCT_ID}"
    const val CREATE_MOVEMENT = "create_movement?$ARG_PREFILLED_PRODUCT_ID={$ARG_PREFILLED_PRODUCT_ID}"
    const val PRODUCT_FORM = "product_form?$ARG_EDIT_PRODUCT_ID={$ARG_EDIT_PRODUCT_ID}"
    const val EDIT_USER_ACCESS = "edit_user_access/{$ARG_USER_ID}"
    const val LOCATION_FORM = "location_form?$ARG_EDIT_LOCATION_ID={$ARG_EDIT_LOCATION_ID}"
    const val SUPPLIER_FORM = "supplier_form?$ARG_EDIT_SUPPLIER_ID={$ARG_EDIT_SUPPLIER_ID}"

    fun productDetail(id: Int) = "product_detail/$id"

    fun createMovement(productId: Int? = null): String =
        if (productId != null) "create_movement?$ARG_PREFILLED_PRODUCT_ID=$productId" else "create_movement"

    fun productForm(editProductId: Int? = null): String =
        if (editProductId != null) "product_form?$ARG_EDIT_PRODUCT_ID=$editProductId" else "product_form"

    fun editUserAccess(userId: Int) = "edit_user_access/$userId"

    fun locationForm(editLocationId: Int? = null): String =
        if (editLocationId != null) "location_form?$ARG_EDIT_LOCATION_ID=$editLocationId" else "location_form"

    fun supplierForm(editSupplierId: Int? = null): String =
        if (editSupplierId != null) "supplier_form?$ARG_EDIT_SUPPLIER_ID=$editSupplierId" else "supplier_form"
}

package com.wutsi.application.web.model

data class CreateOrderModel(
    val productId: Long = -1,
    val quantity: Int = 0,
    val email: String = "",
    val displayName: String = "",
    val notes: String = "",
    val businessId: Long
)

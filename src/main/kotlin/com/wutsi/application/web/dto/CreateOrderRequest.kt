package com.wutsi.application.web.dto

data class CreateOrderRequest(
    val productId: Long = -1,
    val quantity: Int = 0,
    val email: String = "",
    val displayName: String = "",
    val notes: String = "",
    val businessId: Long,
)

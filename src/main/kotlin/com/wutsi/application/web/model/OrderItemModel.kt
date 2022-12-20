package com.wutsi.application.web.model

data class OrderItemModel(
    val productId: Long,
    val title: String,
    val pictureUrl: String?,
    val quantity: Int,
    val unitPrice: String,
)

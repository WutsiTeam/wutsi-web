package com.wutsi.application.web.model

data class OrderModel(
    val id: String,
    val businessId: Long,
    val customerName: String,
    val customerEmail: String,
    val totalPrice: String,
    val totalDiscount: String,
    val items: List<OrderItemModel>
)

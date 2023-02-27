package com.wutsi.application.web.model

data class OrderModel(
    val id: String,
    val business: BusinessModel,
    val customerName: String,
    val customerEmail: String,
    val totalPrice: String,
    val totalPriceValue: Long,
    val totalDiscount: String,
    val items: List<OrderItemModel>,
)

package com.wutsi.application.web.model

data class BusinessModel(
    val id: Long = -1,
    val country: String = "",
    val currency: String? = null,
    val totalSales: Long = 0,
    val totalOrders: Long = 0,
)

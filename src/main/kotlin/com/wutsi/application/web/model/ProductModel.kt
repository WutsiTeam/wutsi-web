package com.wutsi.application.web.model

data class ProductModel(
    val id: Long = -1,
    val title: String = "",
    val price: String? = null,
    val thumbnailUrl: String? = null
)

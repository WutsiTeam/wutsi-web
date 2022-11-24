package com.wutsi.application.web.model

data class ProductModel(
    val id: Long = -1,
    val title: String = "",
    val price: String? = null,
    val thumbnailUrl: String? = null,
    val summary: String? = null,
    val description: String? = null,
    val quantity: Int? = null,
    val pictures: List<PictureModel> = emptyList(),
    val url: String = ""
)

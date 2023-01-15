package com.wutsi.application.web.model

data class ProductModel(
    val id: Long,
    val title: String,
    val price: String?,
    val thumbnailUrl: String?,
    val quantity: Int?,
    val url: String,
    val outOfStock: Boolean,
    val lowStock: Boolean,
    val summary: String?,
    val type: String,
    val event: EventModel?,
    val description: String? = null,
    val pictures: List<PictureModel> = emptyList(),
    val fileTypes: List<FileType> = emptyList(),
)

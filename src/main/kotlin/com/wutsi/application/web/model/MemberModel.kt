package com.wutsi.application.web.model

data class MemberModel(
    val id: Long = -1,
    val displayName: String = "",
    val category: String? = null,
    val pictureUrl: String? = null,
    val location: String? = null,
    val biography: String? = null
)

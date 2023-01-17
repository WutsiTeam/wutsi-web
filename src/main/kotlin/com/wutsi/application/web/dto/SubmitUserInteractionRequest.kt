package com.wutsi.application.web.dto

data class SubmitUserInteractionRequest(
    val time: Long = -1,
    val hitId: String = "",
    val page: String = "",
    val event: String? = null,
    val value: String? = null,
    val productId: String? = null,
    val ua: String? = null,
    val url: String? = null,
    var businessId: String? = null,
)

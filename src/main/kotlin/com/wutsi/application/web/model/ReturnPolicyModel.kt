package com.wutsi.application.web.model

public data class ReturnPolicyModel(
    public val accepted: Boolean = false,
    public val contactWindowDays: Int = 0,
    public val shipBackWindowDays: Int = 0,
    public val message: String? = null,
)

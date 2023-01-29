package com.wutsi.application.web.model

public data class CancellationPolicyModel(
    public val accepted: Boolean = false,
    public val windowHours: Int = 0,
    public val message: String? = null,
)

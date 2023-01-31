package com.wutsi.application.web.service.recaptcha

data class RecaptchaResponse(
    val success: Boolean = false,
    val challenge_ts: String = "",
    val hostname: String = "",
    val errors: List<String> = emptyList(),
)

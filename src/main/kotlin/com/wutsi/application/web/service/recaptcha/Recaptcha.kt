package com.wutsi.application.web.service.recaptcha

import com.wutsi.platform.core.logging.KVLogger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class Recaptcha(
    private val logger: KVLogger,
    @Value("\${wutsi.application.google.recaptcha.secret-key}") val recaptchaSecretKey: String,
) {
    companion object {
        const val REQUEST_PARAMETER = "g-recaptcha-response"
        private const val URL = "https://www.google.com/recaptcha/api/siteverify"
        private val LOGGER = LoggerFactory.getLogger(Recaptcha::class.java)
    }

    private val rest = RestTemplate()

    fun verify(recaptchaResponse: String): Boolean {
        try {
            val url = "$URL?secret=$recaptchaSecretKey&response=$recaptchaResponse"
            val response = rest.postForEntity(url, null, RecaptchaResponse::class.java)

            logger.add("recaptcha_status", response.body?.success)
            logger.add("recaptcha_errors", response.body?.errors)
            return response.body?.success ?: false
        } catch (ex: Exception) {
            LOGGER.warn("Unexpected error while validating recaptcha", ex)
            return false
        }
    }
}

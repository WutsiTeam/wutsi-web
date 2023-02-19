package com.wutsi.application.web.endpoint

import com.wutsi.application.web.Page
import com.wutsi.application.web.model.PageModel
import com.wutsi.membership.manager.dto.Member
import org.slf4j.LoggerFactory
import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import javax.servlet.http.HttpServletRequest

@Controller
class WutsiErrorController : ErrorController, AbstractController() {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(WutsiErrorController::class.java)

        const val WUTSI_MERCHANT_ID = "com.wutsi.merchant_id"
        const val WUTSI_DOWNLOAD_ERROR = "com.wutsi.download_error"
        const val WUTSI_PRODUCT_ID = "com.wutsi.product_id"
        const val WUTSI_FILE_ID = "com.wutsi.file_id"
    }

    @GetMapping("/error")
    fun error(request: HttpServletRequest, model: Model): String {
        model.addAttribute("page", createPage())

        // Error
        logger.add("error_message", request.getAttribute("javax.servlet.error.message"))
        val exception = request.getAttribute("javax.servlet.error.exception") as Throwable?
        if (exception != null) {
            logger.setException(exception)
        }

        // Status code
        model.addAttribute("statusCode", request.getAttribute("javax.servlet.error.status_code"))

        // Download errors?
        model.addAttribute("downloadError", request.getAttribute(WUTSI_DOWNLOAD_ERROR))

        // Merchant Link
        val merchantId = request.getAttribute(WUTSI_MERCHANT_ID) as Long?
        if (merchantId != null) {
            model.addAttribute("merchant", getMerchant(merchantId)?.let { mapper.toMemberModel(it) })
        }

        // Retry
        val retryUrl = if (request.queryString.isNullOrEmpty()) {
            request.requestURL
        } else {
            "${request.requestURL}?${request.queryString}"
        }
        model.addAttribute("retryUrl", retryUrl)

        return "http_error"
    }

    private fun getMerchant(memberId: Long): Member? =
        try {
            resolveCurrentMerchant(memberId)
        } catch (ex: Exception) {
            LOGGER.warn("Unable to resolve user#$memberId", ex)
            null
        }

    private fun createPage() = PageModel(
        name = Page.ERROR,
        title = "Error",
        robots = "noindex",
    )
}

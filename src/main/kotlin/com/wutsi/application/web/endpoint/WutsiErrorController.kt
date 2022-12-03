package com.wutsi.application.web.endpoint

import com.wutsi.application.web.Page
import com.wutsi.application.web.model.PageModel
import com.wutsi.platform.core.logging.KVLogger
import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import javax.servlet.http.HttpServletRequest

@Controller
class WutsiErrorController(
    private val logger: KVLogger
) : ErrorController, AbstractController() {
    @GetMapping("/error")
    fun error(request: HttpServletRequest, model: Model): String {
        model.addAttribute("page", createPage())

        logger.add("error_message", request.getAttribute("javax.servlet.error.message"))

        val exception = request.getAttribute("javax.servlet.error.exception") as Throwable?
        if (exception != null) {
            logger.setException(exception)
        }

        val code = request.getAttribute("javax.servlet.error.status_code") as Int?
        logger.add("status_code", code)
        return if (code == 404) {
            "error/404"
        } else {
            "error/500"
        }
    }

    private fun createPage() = PageModel(
        name = Page.ERROR,
        title = "Error",
        robots = "noindex"
    )
}

package com.wutsi.application.web.servlet

import com.amazonaws.util.IOUtils
import com.wutsi.application.web.service.EnvironmentDetector
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import java.io.ByteArrayInputStream
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@WebServlet(urlPatterns = ["/robots.txt"])
class RobotsServlet(private val env: EnvironmentDetector) : HttpServlet() {
    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        val content = if (env.prod()) {
            """
                User-agent: *
                Allow: /
            """.trimIndent()
        } else {
            """
                User-agent: *
                Disallow: /
            """.trimIndent()
        }

        resp.addHeader(HttpHeaders.CONTENT_TYPE, "text/plain")
        resp.status = HttpStatus.OK.value()
        IOUtils.copy(ByteArrayInputStream(content.toByteArray()), resp.outputStream)
    }
}

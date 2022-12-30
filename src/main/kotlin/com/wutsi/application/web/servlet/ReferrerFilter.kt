package com.wutsi.application.web.servlet

import com.wutsi.platform.core.logging.KVLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Service
class ReferrerFilter(
    private val logger: KVLogger,
    @Value("\${wutsi.application.server-url}") private val serverUrl: String,
) : Filter {
    companion object {
        const val COOKIE = "rfrr"
    }

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        filter(request as HttpServletRequest, response as HttpServletResponse, chain)
    }

    private fun filter(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        try {
            val referrer = request.getHeader(HttpHeaders.REFERER)
            val forwardedFor = request.getHeader("X-Forwarded-For")
            logger.add("referrer", referrer)
            logger.add("x-forwarded-for", forwardedFor)
        } finally {
            chain.doFilter(request, response)
        }
    }
}

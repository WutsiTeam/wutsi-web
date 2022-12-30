package com.wutsi.application.web.servlet

import com.wutsi.platform.core.logging.KVLogger
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Filter to identify the source of traffic
 */
@Service
class ReferrerFilter(
    private val logger: KVLogger,
) : Filter {
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        filter(request as HttpServletRequest, response as HttpServletResponse, chain)
    }

    private fun filter(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        try {
            val referrer = request.getHeader(HttpHeaders.REFERER)
            val origin = request.getHeader(HttpHeaders.ORIGIN)
            logger.add("referrer", referrer)
            logger.add("origin", origin)
        } finally {
            response.addHeader("Referrer-Policy", "strict-origin-when-cross-origin")
            chain.doFilter(request, response)
        }
    }
}

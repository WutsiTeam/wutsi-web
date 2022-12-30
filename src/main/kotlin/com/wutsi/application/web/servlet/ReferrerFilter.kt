package com.wutsi.application.web.servlet

import com.wutsi.platform.core.logging.KVLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Filter to identify the source of traffic
 */
@Service
class ReferrerFilter(
    private val logger: KVLogger,
    @Value("\${wutsi.application.server-url}") private val serverUrl: String,
) : Filter {
    companion object {
        const val RFRR_COOKIE = "rfrr"
    }

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        filter(request as HttpServletRequest, response as HttpServletResponse, chain)
    }

    private fun filter(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        try {
            val referrer = request.getHeader(HttpHeaders.REFERER)
            val forwardedIP = request.getHeader("X-Forwarded-For")
            val forwardedHost = request.getHeader("X-Forwarded-Host")
            val forwardedProto = request.getHeader("X-Forwarded-Proto")
            logger.add("referrer", referrer)
            logger.add("x-forwarded-for", forwardedIP)
            logger.add("x-forwarded-host", forwardedHost)
            logger.add("x-forwarded-proto", forwardedProto)

            if (!referrer.startsWith(serverUrl)) {
                var cookie = getCookie(request)
                if (cookie == null) {
                    cookie = Cookie(RFRR_COOKIE, referrer)
                    cookie.path = "/"
                    cookie.maxAge = 86400
                    response.addCookie(cookie)
                } else {
                    cookie.value = referrer
                }
            }
        } finally {
            chain.doFilter(request, response)
        }
    }

    private fun getCookie(request: HttpServletRequest): Cookie? =
        request.cookies?.find { it.name == RFRR_COOKIE }
}

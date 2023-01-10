package com.wutsi.application.web.servlet

import com.wutsi.enums.ChannelType
import com.wutsi.enums.util.ChannelDetector
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
class ChannelFilter(
    private val logger: KVLogger,
    @Value("\${wutsi.application.server-url}") private val serverUrl: String,
) : Filter {
    companion object {
        const val CHANNEL_COOKIE = "channel"
    }

    private val detector = ChannelDetector()

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        filter(request as HttpServletRequest, response as HttpServletResponse, chain)
    }

    private fun filter(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        try {
            val channel = getChannelType(request)
            logger.add("channel", channel)
            if (channel != null) {
                var cookie = getCookie(request)
                if (cookie == null) {
                    cookie = Cookie(CHANNEL_COOKIE, channel.name)
                } else if (cookie.value != channel.name) {
                    cookie.value = channel.name
                }
                cookie.path = "/"
                cookie.maxAge = 86400
                response.addCookie(cookie)
            }
        } finally {
            chain.doFilter(request, response)
        }
    }

    private fun getChannelType(request: HttpServletRequest): ChannelType? {
        val referer = request.getHeader(HttpHeaders.REFERER)
        return if (referer != null && !referer.startsWith(serverUrl)) {
            detector.detect(
                url = getRequestURL(request),
                referer = referer,
                ua = request.getHeader(HttpHeaders.USER_AGENT),
            )
        } else {
            null
        }
    }

    private fun getRequestURL(request: HttpServletRequest): String {
        val requestURL = StringBuilder(request.requestURL.toString())
        val queryString = request.queryString
        return if (queryString == null) {
            requestURL.toString()
        } else {
            requestURL.append('?').append(queryString).toString()
        }
    }

    private fun getCookie(request: HttpServletRequest): Cookie? =
        request.cookies?.find { it.name == CHANNEL_COOKIE }
}

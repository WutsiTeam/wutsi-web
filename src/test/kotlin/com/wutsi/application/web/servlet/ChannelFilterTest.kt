package com.wutsi.application.web.servlet

import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.enums.ChannelType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import javax.servlet.FilterChain
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.test.assertEquals

internal class ChannelFilterTest {
    companion object {
        const val UA = "Mozilla/5.0 (iPhone; CPU iPhone OS 15_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko)"
    }

    private lateinit var request: HttpServletRequest
    private lateinit var response: HttpServletResponse
    private lateinit var chain: FilterChain

    private val filter = ChannelFilter(mock(), "https://www.wutsi.me")

    @BeforeEach
    fun setUp() {
        request = mock()
        response = mock()
        chain = mock()

        doReturn("https://www.google.com").whenever(request).getHeader(HttpHeaders.REFERER)
        doReturn(UA).whenever(request).getHeader(HttpHeaders.USER_AGENT)
        doReturn(StringBuffer("https://www.wutsi.me/p/1")).whenever(request).requestURL
    }

    @Test
    fun `add cookie`() {
        filter.doFilter(request, response, chain)

        val cookie = argumentCaptor<Cookie>()
        verify(response).addCookie(cookie.capture())
        assertEquals(ChannelFilter.CHANNEL_COOKIE, cookie.firstValue.name)
        assertEquals(ChannelType.SEO.name, cookie.firstValue.value)
        assertEquals("/", cookie.firstValue.path)
        assertEquals(86400, cookie.firstValue.maxAge)

        verify(chain).doFilter(request, response)
    }

    @Test
    fun `update cookie`() {
        val cookie = Cookie(ChannelFilter.CHANNEL_COOKIE, ChannelType.WEB.name)
        doReturn(arrayOf(cookie)).whenever(request).cookies

        filter.doFilter(request, response, chain)

        assertEquals(ChannelType.SEO.name, cookie.value)
        verify(chain).doFilter(request, response)
    }

    @Test
    fun `internal redirection`() {
        val cookie = Cookie(ChannelFilter.CHANNEL_COOKIE, ChannelType.WEB.name)
        doReturn(arrayOf(cookie)).whenever(request).cookies
        doReturn("https://www.wutsi.me/").whenever(request).getHeader(HttpHeaders.REFERER)

        filter.doFilter(request, response, chain)

        assertEquals(ChannelType.WEB.name, cookie.value)
        verify(chain).doFilter(request, response)
    }
}

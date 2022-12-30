package com.wutsi.application.web.servlet

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.web.service.EnvironmentDetector
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.server.LocalServerPort
import java.net.URL
import kotlin.test.assertEquals

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class RobotsServletTest {
    @LocalServerPort
    protected val port: Int = 0

    @MockBean
    private lateinit var env: EnvironmentDetector

    private fun url() = "http://localhost:$port/robots.txt"

    @Test
    fun prod() {
        // GIVEN
        doReturn(true).whenever(env).prod()

        // WHEN
        val content = URL(url()).readText()

        // WHEN
        assertEquals(
            """
                User-agent: *
                Allow: /
            """.trimIndent(),
            content.trimIndent(),
        )
    }

    @Test
    fun notProd() {
        // GIVEN
        doReturn(false).whenever(env).prod()

        // WHEN
        val content = URL(url()).readText()

        // WHEN
        assertEquals(
            """
                User-agent: *
                Disallow: /
            """.trimIndent(),
            content.trimIndent(),
        )
    }
}

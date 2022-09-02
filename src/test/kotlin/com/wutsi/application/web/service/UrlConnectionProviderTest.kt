package com.wutsi.application.web.service

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.shared.service.TenantProvider
import com.wutsi.platform.core.security.TokenProvider
import com.wutsi.platform.core.tracing.TracingContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class UrlConnectionProviderTest {
    private lateinit var tokenProvider: TokenProvider
    private lateinit var traceContext: TracingContext
    private lateinit var tenantProvider: TenantProvider
    private lateinit var provider: UrlConnectionProvider

    @Test
    fun openConnection() {
        // GIVEN
        tokenProvider = mock()
        doReturn("the-token").whenever(tokenProvider).getToken()

        traceContext = mock()
        doReturn("device-id").whenever(traceContext).deviceId()
        doReturn("client-id").whenever(traceContext).clientId()
        doReturn("client-info").whenever(traceContext).clientInfo()
        doReturn("trace-id").whenever(traceContext).traceId()
        doReturn("555").whenever(traceContext).tenantId()

        tenantProvider = mock()
        doReturn(1L).whenever(tenantProvider).tenantId()

        provider = UrlConnectionProvider(tokenProvider, traceContext, tenantProvider)

        // WHEN
        val cnn = provider.openConnection("https://www.google.ca")

        print(cnn.requestProperties)

        assertEquals("device-id", cnn.getRequestProperty(TracingContext.HEADER_DEVICE_ID))
        assertEquals("trace-id", cnn.getRequestProperty(TracingContext.HEADER_TRACE_ID))
        assertEquals("client-id", cnn.getRequestProperty(TracingContext.HEADER_CLIENT_ID))
        assertEquals("client-info", cnn.getRequestProperty(TracingContext.HEADER_CLIENT_INFO))
        assertEquals("trace-id", cnn.getRequestProperty(TracingContext.HEADER_HEROKU_REQUEST_ID))
        assertEquals("1", cnn.getRequestProperty(TracingContext.HEADER_TENANT_ID))
//        assertEquals("Bearer the-token", cnn.getRequestProperty("Authorization"))
    }
}

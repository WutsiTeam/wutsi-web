package com.wutsi.application.web.service

import com.wutsi.application.shared.service.TenantProvider
import com.wutsi.platform.core.security.TokenProvider
import com.wutsi.platform.core.tracing.TracingContext
import org.springframework.stereotype.Service
import java.net.HttpURLConnection
import java.net.URL

@Service
class UrlConnectionProvider(
    private val tokenProvider: TokenProvider,
    private val traceContext: TracingContext,
    private val tenantProvider: TenantProvider
) {
    fun openConnection(address: String): HttpURLConnection {
        val url = URL(address)
        val cnn = url.openConnection() as HttpURLConnection
        cnn.setRequestProperty("Authorization", "Bearer ${tokenProvider.getToken()}")
        cnn.setRequestProperty(TracingContext.HEADER_TENANT_ID, tenantProvider.tenantId().toString())
        cnn.setRequestProperty(TracingContext.HEADER_CLIENT_INFO, traceContext.clientInfo())
        cnn.setRequestProperty(TracingContext.HEADER_CLIENT_ID, traceContext.clientId())
        cnn.setRequestProperty(TracingContext.HEADER_TRACE_ID, traceContext.traceId())
        cnn.setRequestProperty(TracingContext.HEADER_DEVICE_ID, traceContext.deviceId())
        cnn.setRequestProperty(TracingContext.HEADER_HEROKU_REQUEST_ID, traceContext.traceId())
        return cnn
    }
}

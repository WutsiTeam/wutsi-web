package com.wutsi.application.web.endpoint

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.web.Page
import com.wutsi.application.web.dto.SubmitUserInteractionRequest
import com.wutsi.event.EventURN
import com.wutsi.event.TrackEventPayload
import com.wutsi.platform.core.stream.EventStream
import com.wutsi.platform.core.tracing.TracingContext
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.web.client.RestTemplate
import java.util.UUID
import kotlin.test.assertEquals

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class TrackControllerTest {
    @LocalServerPort
    protected val port: Int = 0

    @MockBean
    private lateinit var eventStream: EventStream

    @MockBean
    private lateinit var tracingContext: TracingContext

    private val rest = RestTemplate()

    @Test
    fun index() {
        // GIVEN
        val deviceId = UUID.randomUUID().toString()
        doReturn(deviceId).whenever(tracingContext).deviceId()

        // WHEN
        val request = SubmitUserInteractionRequest(
            hitId = UUID.randomUUID().toString(),
            page = Page.PRODUCT,
            event = "load",
            value = "xxxx",
            productId = "1111",
            time = System.currentTimeMillis(),
            url = "https://wutsi.com/p/1/30493-43094",
            ua = "43094309",
            businessId = "111",
        )
        val response = rest.postForEntity(url(), request, Any::class.java)

        // THEN
        assertEquals(HttpStatus.OK, response.statusCode)

        verify(eventStream).publish(
            EventURN.TRACK.urn,
            TrackEventPayload(
                time = request.time,
                correlationId = request.hitId,
                page = request.page,
                value = request.value,
                productId = request.productId,
                url = request.url,
                ua = request.ua,
                event = request.event,
                deviceId = deviceId,
                businessId = request.businessId,
            ),
        )
    }

    private fun url() = "http://localhost:$port/track"
}

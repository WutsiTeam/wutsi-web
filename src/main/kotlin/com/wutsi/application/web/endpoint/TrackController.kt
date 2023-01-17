package com.wutsi.application.web.endpoint

import com.wutsi.application.web.dto.SubmitUserInteractionRequest
import com.wutsi.application.web.servlet.ReferrerFilter
import com.wutsi.event.EventURN
import com.wutsi.event.TrackEventPayload
import com.wutsi.platform.core.stream.EventStream
import com.wutsi.platform.core.tracing.TracingContext
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

@RestController
@RequestMapping("/track")
class TrackController(
    private val eventStream: EventStream,
    private val tracingContext: TracingContext,
) : AbstractController() {
    @PostMapping
    fun index(@RequestBody request: SubmitUserInteractionRequest) {
        logger.add("request_ua", request.ua)
        logger.add("request_url", request.url)
        logger.add("request_hit_id", request.hitId)
        logger.add("request_page", request.page)
        logger.add("request_event", request.event)
        logger.add("request_value", request.value)
        logger.add("request_time", request.time)
        logger.add("request_business_id", request.businessId)
        logger.add("request_product_id", request.productId)

        val httpRequest = (RequestContextHolder.getRequestAttributes() as ServletRequestAttributes).request
        eventStream.publish(
            type = EventURN.TRACK.urn,
            payload = TrackEventPayload(
                time = request.time,
                url = request.url,
                ua = request.ua,
                event = request.event,
                value = request.value,
                productId = request.productId,
                correlationId = request.hitId,
                page = request.page,
                deviceId = tracingContext.deviceId(),
                referrer = httpRequest.cookies?.find { it.name == ReferrerFilter.RFRR_COOKIE }?.value,
                businessId = request.businessId,
            ),
        )
    }
}

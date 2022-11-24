package com.wutsi.application.web.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.mobile.device.DeviceResolverRequestFilter

@Configuration
class DeviceDetectorConfiguration {
    @Bean
    fun deviceResolverRequestFilter(): DeviceResolverRequestFilter =
        DeviceResolverRequestFilter()
}

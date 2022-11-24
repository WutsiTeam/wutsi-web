package com.wutsi.application.web.endpoint

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.mobile.device.Device
import org.springframework.mobile.device.DeviceUtils
import org.springframework.web.bind.annotation.ModelAttribute
import javax.servlet.http.HttpServletRequest

abstract class AbstractController {
    @Value("\${wutsi.application.asset-url}")
    protected lateinit var assetUrl: String

    @Value("\${wutsi.application.server-url}")
    protected lateinit var serverUrl: String

    @Autowired
    protected lateinit var request: HttpServletRequest

    @ModelAttribute("device")
    fun device(): Device = DeviceUtils.getCurrentDevice(request)

    @ModelAttribute("assetUrl")
    fun assetUrl() = assetUrl
}

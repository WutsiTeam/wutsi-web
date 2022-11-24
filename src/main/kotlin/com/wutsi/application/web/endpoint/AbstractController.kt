package com.wutsi.application.web.endpoint

import org.springframework.beans.factory.annotation.Value

abstract class AbstractController {
    @Value("\${wutsi.application.asset-url}")
    protected lateinit var assetUrl: String
}

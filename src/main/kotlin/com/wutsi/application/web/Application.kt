package com.wutsi.application.web

import com.wutsi.platform.core.WutsiApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@WutsiApplication
// @WutsiBffApplication
@SpringBootApplication
class Application

fun main(vararg args: String) {
    org.springframework.boot.runApplication<Application>(*args)
}

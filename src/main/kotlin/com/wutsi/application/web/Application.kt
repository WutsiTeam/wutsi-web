package com.wutsi.application.web

import com.wutsi.platform.core.WutsiApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.web.servlet.ServletComponentScan

@WutsiApplication
@SpringBootApplication
@ServletComponentScan
class Application

fun main(vararg args: String) {
    org.springframework.boot.runApplication<Application>(*args)
}

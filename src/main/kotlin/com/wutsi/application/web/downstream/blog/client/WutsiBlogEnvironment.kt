package com.wutsi.application.web.downstream.blog.client

enum class WutsiBlogEnvironment(
    val url: String
) {
    PRODUCTION("https://com-wutsi-blog.herokuapp.com"),
    SANDBOX("https://int-com-wutsi-blog.herokuapp.com")
}

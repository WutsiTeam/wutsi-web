package com.wutsi.application.web.model

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "urlset")
@XmlAccessorType(XmlAccessType.FIELD)
data class SitemapModel(
    @XmlElement val url: List<UrlModel> = mutableListOf(),
)

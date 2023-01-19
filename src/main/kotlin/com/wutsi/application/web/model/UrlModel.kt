package com.wutsi.application.web.model

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "url")
@XmlAccessorType(XmlAccessType.FIELD)
class UrlModel(
    @XmlElement val loc: String = "",
    @XmlElement val lastmod: String? = null,
)

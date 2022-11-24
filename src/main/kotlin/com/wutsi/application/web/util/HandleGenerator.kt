package com.wutsi.application.web.util

import java.text.Normalizer

object HandleGenerator {
    private val FILTER1 = "\\s+|-+|\\p{Punct}".toRegex()
    private val FILTER2 = "[-*]{2,}".toRegex()
    private val SEPARATOR = "-"

    fun generate(name: String): String {
        var xname = toAscii(name)
            .replace(FILTER1, SEPARATOR)
            .replace(FILTER2, SEPARATOR)
            .lowercase()
        if (xname.endsWith(SEPARATOR)) {
            xname = xname.substring(0, xname.length - 1)
        }
        if (xname.startsWith(SEPARATOR)) {
            xname = xname.substring(1)
        }

        return xname
    }

    private fun toAscii(string: String): String {
        var str = string.trim()
        val sb = StringBuilder(str.length)
        str = Normalizer.normalize(str, Normalizer.Form.NFD)
        for (c in str.toCharArray()) {
            if (c <= '\u007F') sb.append(c)
        }
        return sb.toString()
    }
}

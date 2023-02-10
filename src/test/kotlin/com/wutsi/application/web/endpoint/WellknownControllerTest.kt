package com.wutsi.application.web.endpoint

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import java.net.URL
import kotlin.test.assertEquals

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class WellknownControllerTest {
    @LocalServerPort
    protected val port: Int = 0

    @Test
    fun assetlinks() {
        val txt = URL(url("assetlinks.json")).readText()

        assertEquals(
            """
                [
                  {
                    "relation": [
                      "delegate_permission/common.handle_all_urls"
                    ],
                    "target": {
                      "namespace": "android_app",
                      "package_name": "com.wutsi.wutsi_wallet",
                      "sha256_cert_fingerprints": [
                        "F9:D0:59:3D:3A:71:8C:30:40:B3:F4:A3:4F:E5:0A:F8:8B:EA:0A:56:04:7B:3A:87:36:DB:6C:95:74:1C:AF:C4"
                      ]
                    }
                  }
                ]

            """.trimIndent(),
            txt
        )
    }

    private fun url(path: String): String = "http://localhost:$port/.well-known/$path"
}

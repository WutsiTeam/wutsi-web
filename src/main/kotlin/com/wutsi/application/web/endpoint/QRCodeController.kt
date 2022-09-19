package com.wutsi.application.web.endpoint

import com.wutsi.application.shared.service.TenantProvider
import com.wutsi.platform.core.qrcode.KeyProvider
import com.wutsi.platform.core.qrcode.QrCode
import com.wutsi.platform.core.qrcode.QrCodeImageGenerator
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpHeaders.CONTENT_DISPOSITION
import org.springframework.http.MediaType.IMAGE_PNG_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import java.io.ByteArrayOutputStream
import java.net.URL
import java.util.UUID

@Controller
@RequestMapping("/qr-code")
class QRCodeController(
    private val keyProvider: KeyProvider,
    private val tenantProvider: TenantProvider
) {
    @GetMapping("/{type}/{id}.png")
    fun account(@PathVariable type: String, @PathVariable id: Long): ResponseEntity<ByteArrayResource> {
        val tenant = tenantProvider.get()
        val data = QrCode(type = type.uppercase(), value = id.toString()).encode(keyProvider)

        val logoUrl = tenantProvider.logo(tenant)?.let { URL(it) }
        val image = ByteArrayOutputStream()
        QrCodeImageGenerator(logoUrl).generate(data, image)

        val resource = ByteArrayResource(image.toByteArray(), IMAGE_PNG_VALUE)
        return ResponseEntity.ok()
            .header(CONTENT_DISPOSITION, "attachment; filename=\"qrcode-$id-${UUID.randomUUID()}.png\"")
            .body(resource)
    }
}

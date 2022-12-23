package com.wutsi.application.web.endpoint

import com.wutsi.enums.ProductType
import com.wutsi.error.ErrorURN
import com.wutsi.platform.core.error.Error
import com.wutsi.platform.core.error.exception.NotFoundException
import com.wutsi.platform.core.storage.StorageService
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import java.net.URL
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Controller
class DownloadController(
    private val storageService: StorageService,
) : AbstractController() {
    @GetMapping("/download")
    fun index(
        @RequestParam(name = "o") orderId: String,
        @RequestParam(name = "p") productId: Long,
        @RequestParam(name = "f") fileId: Long,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        // Get order item
        val order = checkoutManagerApi.getOrder(orderId).order
        val orderItem =
            order.items.find { (it.productId == productId) || (it.productType == ProductType.DIGITAL_DOWNLOAD.name) }
                ?: throw NotFoundException(
                    error = Error(
                        code = ErrorURN.ORDER_PRODUCT_NOT_FOUND.urn,
                    ),
                )

        // Get product
        val product = marketplaceManagerApi.getProduct(orderItem.productId).product
        try {
            // File
            val file = product.files.find { it.id == fileId }
                ?: throw NotFoundException(
                    error = Error(
                        code = ErrorURN.PRODUCT_FILE_NOT_FOUND.urn,
                    ),
                )

            // Download
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${file.name}\"")
            response.setHeader(HttpHeaders.CONTENT_TYPE, file.contentType)
            response.setContentLength(file.contentSize)
            storageService.get(URL(file.url), response.outputStream)
        } catch (ex: Throwable) {
            request.setAttribute(WutsiErrorController.WUTSI_MERCHANT_ID, order.business.accountId)
            request.setAttribute(WutsiErrorController.WUTSI_DOWNLOAD_ERROR, true)
            request.setAttribute(WutsiErrorController.WUTSI_PRODUCT_ID, productId)
            request.setAttribute(WutsiErrorController.WUTSI_FILE_ID, fileId)
            throw ex
        }
    }
}

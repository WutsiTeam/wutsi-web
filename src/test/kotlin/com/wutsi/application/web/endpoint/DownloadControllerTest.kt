package com.wutsi.application.web.endpoint

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.web.Fixtures
import com.wutsi.checkout.manager.CheckoutManagerApi
import com.wutsi.checkout.manager.dto.GetOrderResponse
import com.wutsi.checkout.manager.dto.OrderItem
import com.wutsi.enums.ProductType
import com.wutsi.marketplace.manager.MarketplaceManagerApi
import com.wutsi.marketplace.manager.dto.GetProductResponse
import com.wutsi.platform.core.storage.StorageService
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpHeaders
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.test.assertEquals

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class DownloadControllerTest {
    @LocalServerPort
    protected val port: Int = 0

    @MockBean
    private lateinit var storageService: StorageService

    @MockBean
    protected lateinit var marketplaceManagerApi: MarketplaceManagerApi

    @MockBean
    protected lateinit var checkoutManagerApi: CheckoutManagerApi

    @Test
    fun index() {
        // GIVEN
        val product = Fixtures.createProduct(
            id = 111L,
            type = ProductType.DIGITAL_DOWNLOAD,
            files = listOf(
                Fixtures.createFileSummary(
                    id = 1,
                    name = "Logo.png",
                    contentSize = 10,
                    contentType = "image/png",
                    url = "https://www.google.ca/f/logo.png",
                ),
            ),
        )
        doReturn(GetProductResponse(product)).whenever(marketplaceManagerApi).getProduct(any())

        val order = Fixtures.createOrder(
            id = "1111",
            items = listOf(
                OrderItem(
                    productId = product.id,
                    productType = product.type,
                ),
            ),
        )
        doReturn(GetOrderResponse(order)).whenever(checkoutManagerApi).getOrder(any())

        val content = "1".repeat(product.files[0].contentSize)
        doAnswer { inv ->
            (inv.arguments[1] as OutputStream).write(content.toByteArray())
        }.whenever(storageService).get(any(), any())

        // WHEN/THEN
        val url = "http://localhost:$port/download?p=${product.id}&f=${product.files[0].id}&o=${order.id}"
        val cnn = URL(url).openConnection() as HttpURLConnection
        try {
            assertEquals(200, cnn.responseCode)
            assertEquals(product.files[0].contentSize, cnn.contentLength)
            assertEquals(product.files[0].contentType, cnn.contentType)
            assertEquals(
                "attachment; filename=\"${product.files[0].name}\"",
                cnn.headerFields[HttpHeaders.CONTENT_DISPOSITION]?.get(0),
            )
        } finally {
            cnn.disconnect()
        }
    }

    @Test
    fun notDigitalDownload() {
        // GIVEN
        val product = Fixtures.createProduct(
            id = 111L,
            type = ProductType.DIGITAL_DOWNLOAD,
            files = listOf(
                Fixtures.createFileSummary(
                    id = 1,
                    name = "Logo.png",
                    contentSize = 10,
                    url = "https://www.google.ca/f/logo.png",
                ),
            ),
        )
        doReturn(GetProductResponse(product)).whenever(marketplaceManagerApi).getProduct(any())

        val order = Fixtures.createOrder(
            id = "1111",
            items = listOf(
                OrderItem(
                    productId = product.id,
                    productType = product.type,
                ),
            ),
        )
        doReturn(GetOrderResponse(order)).whenever(checkoutManagerApi).getOrder(any())

        // WHEN/THEN
        val url = "http://localhost:$port/download?p=${product.id}&f=111&o=${order.id}"
        val cnn = URL(url).openConnection() as HttpURLConnection
        try {
            assertEquals(404, cnn.responseCode)
        } finally {
            cnn.disconnect()
        }
    }

    @Test
    fun invalidFileId() {
        // GIVEN
        val product = Fixtures.createProduct(
            id = 111L,
            type = ProductType.EVENT,
        )
        doReturn(GetProductResponse(product)).whenever(marketplaceManagerApi).getProduct(any())

        val order = Fixtures.createOrder(
            id = "1111",
            items = listOf(
                OrderItem(
                    productId = product.id,
                    productType = product.type,
                ),
            ),
        )
        doReturn(GetOrderResponse(order)).whenever(checkoutManagerApi).getOrder(any())

        // WHEN/THEN
        val url = "http://localhost:$port/download?p=${product.id}&f=99999&o=${order.id}"
        val cnn = URL(url).openConnection() as HttpURLConnection
        try {
            assertEquals(404, cnn.responseCode)
        } finally {
            cnn.disconnect()
        }
    }
}

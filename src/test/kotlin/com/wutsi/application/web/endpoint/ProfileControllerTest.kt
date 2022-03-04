package com.wutsi.application.web.endpoint

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.platform.account.WutsiAccountApi
import com.wutsi.platform.account.dto.Account
import com.wutsi.platform.account.dto.Category
import com.wutsi.platform.account.dto.GetAccountResponse
import com.wutsi.platform.qr.WutsiQrApi
import com.wutsi.platform.qr.dto.EncodeQRCodeResponse
import org.junit.jupiter.api.Test
import org.springframework.boot.test.mock.mockito.MockBean

internal class ProfileControllerTest : SeleniumTestSupport() {
    @MockBean
    private lateinit var accountApi: WutsiAccountApi

    @MockBean
    private lateinit var qrApi: WutsiQrApi

    @Test
    fun `user profile`() {
        // GIVEN
        val account = createAccount()
        doReturn(GetAccountResponse(account)).whenever(accountApi).getAccount(any())

        doReturn(EncodeQRCodeResponse("111-222-333")).whenever(qrApi).encode(any())

        // WHEN
        navigate(url("profile?id=${account.id}"))
        Thread.sleep(1000)

        // THEN
        assertCurrentPageIs("page.profile")

        // Header
        assertElementAttribute("head title", "text", "${account.displayName} | Wutsi")
        assertElementAttribute("head meta[name='description']", "content", account.biography)
        assertElementAttribute("head meta[property='og:type']", "content", "profile")
        assertElementAttribute("head meta[property='og:title']", "content", account.displayName)
        assertElementAttribute("head meta[property='og:description']", "content", account.biography)
        assertElementAttribute(
            "head meta[property='og:image']",
            "content",
            "https://wutsi-qr-server-test.herokuapp.com/image/111-222-333.png"
        )

        // Content
        assertElementText(".slide--headline h1", account.displayName!!)
        assertElementText(".slide--bio", account.biography!!)
        assertElementText(".slide--category", account.category!!.title)
        assertElementAttribute("img.qr-code", "src", "https://wutsi-qr-server-test.herokuapp.com/image/111-222-333.png")

        assertElementAttribute(
            ".cta-android",
            "href",
            "https://play.google.com/store/apps/details?id=com.wutsi.wutsi_wallet"
        )
        assertElementNotPresent(".cta-ios")
    }

    private fun createAccount() = Account(
        id = 1,
        displayName = "Ray Sponsible",
        biography = "This is a sample biography of a user",
        country = "CM",
        language = "en",
        pictureUrl = "https://st2.depositphotos.com/1001030/12469/i/950/depositphotos_124693804-stock-photo-afro-american-man-posing-in.jpg",
        business = true,
        category = Category(
            id = 100,
            title = "Writer"
        )
    )
}

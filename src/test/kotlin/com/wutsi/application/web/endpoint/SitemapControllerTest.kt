package com.wutsi.application.web.endpoint

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.web.Fixtures
import com.wutsi.application.web.model.SitemapModel
import com.wutsi.application.web.view.SitemapView
import com.wutsi.enums.ProductStatus
import com.wutsi.marketplace.manager.MarketplaceManagerApi
import com.wutsi.marketplace.manager.dto.SearchProductRequest
import com.wutsi.marketplace.manager.dto.SearchProductResponse
import com.wutsi.membership.manager.MembershipManagerApi
import com.wutsi.membership.manager.dto.GetMemberResponse
import com.wutsi.membership.manager.dto.SearchMemberRequest
import com.wutsi.membership.manager.dto.SearchMemberResponse
import com.wutsi.regulation.RegulationEngine
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.server.LocalServerPort
import java.net.URL
import javax.xml.bind.JAXB
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class SitemapControllerTest {
    @LocalServerPort
    protected val port: Int = 0

    @MockBean
    private lateinit var marketplaceManagerApi: MarketplaceManagerApi

    @MockBean
    private lateinit var membershipManagerApi: MembershipManagerApi

    @Autowired
    private lateinit var regulationEngine: RegulationEngine

    @Test
    fun merchants() {
        // GIVEN
        val members = listOf(
            Fixtures.createMemberSummary(1),
            Fixtures.createMemberSummary(2),
            Fixtures.createMemberSummary(3, "ray.sponsible"),
        )
        doReturn(SearchMemberResponse(members)).whenever(membershipManagerApi).searchMember(any())

        // THEN
        val sitemap = JAXB.unmarshal(URL(url()), SitemapModel::class.java)

        assertEquals(3, sitemap.url.size)
        assertHasUrl("/u/1", sitemap)
        assertHasUrl("/u/2", sitemap)
        assertHasUrl("/@ray.sponsible", sitemap)

        verify(membershipManagerApi).searchMember(
            SearchMemberRequest(
                offset = 0,
                limit = SitemapView.LIMIT,
                business = true,
                store = true,
            ),
        )
    }

    @Test
    fun store() {
        // GIVEN
        val member = Fixtures.createMember(id = 1, business = true, storeId = 11L)
        doReturn(GetMemberResponse(member)).whenever(membershipManagerApi).getMember(any())

        val products = listOf(
            Fixtures.createProductSummary(id = 10L, title = "Hello World"),
            Fixtures.createProductSummary(id = 20L, title = "yo man"),
        )
        doReturn(SearchProductResponse(products)).whenever(marketplaceManagerApi).searchProduct(any())

        // WHEN
        val sitemap = JAXB.unmarshal(URL(url(1L)), SitemapModel::class.java)

        assertEquals(3, sitemap.url.size)
        assertHasUrl("/u/1", sitemap)
        assertHasUrl("/p/10", sitemap)
        assertHasUrl("/p/20", sitemap)

        verify(membershipManagerApi).getMember(member.id)
        verify(marketplaceManagerApi).searchProduct(
            SearchProductRequest(
                storeId = member.storeId,
                limit = regulationEngine.maxProducts(),
                status = ProductStatus.PUBLISHED.name,
            ),
        )
    }

    @Test
    fun noStore() {
        // GIVEN
        val member = Fixtures.createMember(id = 1, business = true, storeId = null)
        doReturn(GetMemberResponse(member)).whenever(membershipManagerApi).getMember(any())

        // WHEN
        val sitemap = JAXB.unmarshal(URL(url(1L)), SitemapModel::class.java)

        assertEquals(1, sitemap.url.size)
        assertHasUrl("/u/1", sitemap)

        verify(membershipManagerApi).getMember(member.id)
        verify(marketplaceManagerApi, never()).searchProduct(any())
    }

    @Test
    fun notBusiness() {
        // GIVEN
        val member = Fixtures.createMember(id = 1, business = false)
        doReturn(GetMemberResponse(member)).whenever(membershipManagerApi).getMember(any())

        // WHEN
        val sitemap = JAXB.unmarshal(URL(url(1L)), SitemapModel::class.java)

        assertEquals(0, sitemap.url.size)
    }

    private fun url(): String = "http://localhost:$port/sitemap.xml"

    private fun url(id: Long): String = "http://localhost:$port/sitemap.xml?id=$id"

    private fun assertHasUrl(url: String, sitemap: SitemapModel) {
        assertNotNull(sitemap.url.find { it.loc.endsWith(url) })
    }
}

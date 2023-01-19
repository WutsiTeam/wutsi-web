package com.wutsi.application.web.view

import com.wutsi.application.web.model.Mapper
import com.wutsi.application.web.model.SitemapModel
import com.wutsi.application.web.model.UrlModel
import com.wutsi.enums.ProductStatus
import com.wutsi.marketplace.manager.MarketplaceManagerApi
import com.wutsi.marketplace.manager.dto.SearchProductRequest
import com.wutsi.membership.manager.MembershipManagerApi
import com.wutsi.regulation.RegulationEngine
import org.springframework.stereotype.Service
import org.springframework.web.servlet.View
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.xml.bind.JAXBContext
import javax.xml.bind.Marshaller

@Service
class SitemapView(
    private val marketplaceManagerApi: MarketplaceManagerApi,
    private val membershipManagerApi: MembershipManagerApi,
    private val regulationEngine: RegulationEngine,
    private val mapper: Mapper,
) : View {
    override fun render(model: MutableMap<String, *>?, request: HttpServletRequest, response: HttpServletResponse) {
        response.contentType = "application/xml"
        response.characterEncoding = "utf-8"

        val sitemap = get(request)
        val jaxbContext = JAXBContext.newInstance(SitemapModel::class.java, UrlModel::class.java)
        val marshaller = jaxbContext.createMarshaller()
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
        marshaller.marshal(sitemap, response.outputStream)
    }

    private fun get(request: HttpServletRequest): SitemapModel {
        val id = request.getParameter("id").toLong()
        val member = membershipManagerApi.getMember(id).member
        val urls = mutableListOf<UrlModel>()

        if (member.business) {
            // Merchant Landing page
            urls.add(mapper.toUrlModel(member))

            // Member Products
            if (member.storeId != null) {
                urls.addAll(
                    marketplaceManagerApi.searchProduct(
                        request = SearchProductRequest(
                            limit = regulationEngine.maxProducts(),
                            status = ProductStatus.PUBLISHED.name,
                            storeId = member.storeId,
                        ),
                    ).products.map { mapper.toUrlModel(it) },
                )
            }
        }

        return SitemapModel(url = urls)
    }
}

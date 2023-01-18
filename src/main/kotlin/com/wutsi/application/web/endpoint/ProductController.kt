package com.wutsi.application.web.endpoint

import com.wutsi.application.web.Page
import com.wutsi.application.web.model.PageModel
import com.wutsi.application.web.model.ProductModel
import com.wutsi.enums.ProductType
import com.wutsi.marketplace.manager.dto.Product
import com.wutsi.membership.manager.dto.Member
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import kotlin.math.min

@Controller
@RequestMapping("/p")
class ProductController : AbstractController() {
    @GetMapping("/{id}")
    fun index(@PathVariable id: Long, model: Model): String {
        val offer = marketplaceManagerApi.getOffer(id).offer
        val merchant = findMember(offer.product.store.accountId)
        val country = regulationEngine.country(merchant.country)

        val offerModel = mapper.toOfferModel(offer, country, merchant)
        model.addAttribute("page", createPage(offerModel.product, merchant))
        model.addAttribute("offer", mapper.toOfferModel(offer, country, merchant))
        model.addAttribute("merchant", mapper.toMemberModel(merchant))

        if (cannotOrderMultipleItems(offer.product)) {
            // Online event, you cannot more buy than 1
        } else if (offer.product.quantity == null || offer.product.quantity!! > 1) {
            val quantities = 1..min(10, (offer.product.quantity ?: Integer.MAX_VALUE))
            model.addAttribute("quantities", quantities)
        }
        return "product"
    }

    @GetMapping("/{id}/{title}")
    fun index2(@PathVariable id: Long, @PathVariable title: String, model: Model): String =
        index(id, model)

    private fun cannotOrderMultipleItems(product: Product): Boolean =
        (product.type == ProductType.EVENT.name) && (product.event?.online == true) ||
            (product.type == ProductType.DIGITAL_DOWNLOAD.name)

    private fun createPage(product: ProductModel, merchant: Member) = PageModel(
        name = Page.PRODUCT,
        title = product.title,
        description = product.summary,
        url = "$serverUrl/${product.url}",
        canonicalUrl = "$serverUrl/p/${product.id}",
        productId = product.id,
        businessId = merchant.businessId,
        imageUrl = product.thumbnailUrl,
    )
}

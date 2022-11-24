package com.wutsi.application.web.endpoint

import com.wutsi.application.web.Page
import com.wutsi.application.web.model.PageModel
import com.wutsi.marketplace.manager.dto.Product
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/p")
class ProductController : AbstractController() {
    @GetMapping("/{id}")
    fun index(@PathVariable id: Long, model: Model): String {
        val product = findProduct(id)
        val store = findStore(product.storeId)
        val merchant = findMember(store.accountId)
        val country = regulationEngine.country(merchant.country)

        model.addAttribute("page", createPage(product))
        model.addAttribute("product", mapper.toProductModel(product, country))
        return "product"
    }

    private fun createPage(product: Product) = PageModel(
        name = Page.PRODUCT,
        title = product.title,
        description = product.summary,
        imageUrl = product.thumbnail?.url,
        assetUrl = assetUrl,
        canonicalUrl = "$serverUrl/p/${product.id}"
    )
}

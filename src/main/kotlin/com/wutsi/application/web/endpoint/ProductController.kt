package com.wutsi.application.web.endpoint

import com.wutsi.application.web.Page
import com.wutsi.application.web.model.PageModel
import com.wutsi.application.web.model.ProductModel
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

        val productModel = mapper.toProductModel(product, country)
        model.addAttribute("page", createPage(productModel))
        model.addAttribute("product", productModel)
        model.addAttribute("merchant", mapper.toMemberModel(merchant))
        return "product"
    }

    @GetMapping("/{id}/{title}")
    fun index2(@PathVariable id: Long, @PathVariable title: String, model: Model): String =
        index(id, model)

    private fun createPage(product: ProductModel) = PageModel(
        name = Page.PRODUCT,
        title = product.title,
        description = product.summary,
        url = "$serverUrl/${product.url}",
        imageUrl = product.thumbnailUrl,
        assetUrl = assetUrl,
        canonicalUrl = "$serverUrl/p/${product.id}"
    )
}

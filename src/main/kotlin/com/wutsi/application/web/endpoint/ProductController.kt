package com.wutsi.application.web.endpoint

import com.wutsi.application.web.Page
import com.wutsi.application.web.model.PageModel
import com.wutsi.application.web.model.ProductModel
import com.wutsi.marketplace.manager.dto.Product
import com.wutsi.platform.core.image.Dimension
import com.wutsi.platform.core.image.ImageService
import com.wutsi.platform.core.image.Transformation
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import kotlin.math.min

@Controller
@RequestMapping("/p")
class ProductController(
    private val imageService: ImageService
) : AbstractController() {
    @GetMapping("/{id}")
    fun index(@PathVariable id: Long, model: Model): String {
        val product = findProduct(id)
        val merchant = findMember(product.store.accountId)
        val country = regulationEngine.country(merchant.country)

        val productModel = mapper.toProductModel(product, country, merchant)
        model.addAttribute("page", createPage(productModel, product))
        model.addAttribute("product", productModel)
        model.addAttribute("merchant", mapper.toMemberModel(merchant))

        if (product.quantity != null && product.quantity!! > 0) {
            val quantities = 1..min(10, (product.quantity ?: Integer.MAX_VALUE))
            model.addAttribute("quantities", quantities)
        }
        return "product"
    }

    @GetMapping("/{id}/{title}")
    fun index2(@PathVariable id: Long, @PathVariable title: String, model: Model): String =
        index(id, model)

    private fun createPage(product: ProductModel, original: Product) = PageModel(
        name = Page.PRODUCT,
        title = product.title,
        description = product.summary,
        url = "$serverUrl/${product.url}",
        canonicalUrl = "$serverUrl/p/${product.id}",
        imageUrl = original.thumbnail?.url?.let {
            imageService.transform(
                url = it,
                transformation = Transformation(
                    dimension = Dimension(
                        600,
                        315
                    ) // See https://developers.facebook.com/docs/sharing/webmasters/images/
                )
            )
        }
    )
}

package com.wutsi.application.web.endpoint

import com.wutsi.ecommerce.catalog.WutsiCatalogApi
import com.wutsi.ecommerce.catalog.dto.Product
import com.wutsi.platform.core.image.Dimension
import com.wutsi.platform.core.image.ImageService
import com.wutsi.platform.core.image.Transformation
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/product")
class ProductController(
    private val catalogApi: WutsiCatalogApi,
    private val imageService: ImageService
) : AbstractPageController() {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(ProductController::class.java)
    }

    override fun pageId() = "page.product"

    @GetMapping
    fun index(@RequestParam id: Long, model: Model): String {
        try {
            val product: Product = findProduct(id)
            model.addAttribute("downloadText", "Install the App to view the Product details")
            addOpenGraph(product, model)
        } catch (ex: Exception) {
            LOGGER.warn("Unexpected error while loading Order#$id", ex)
        } finally {
            return "default"
        }
    }

    private fun addOpenGraph(product: Product, model: Model) {
        model.addAttribute("title", product.title)
        model.addAttribute("description", product.summary)
        model.addAttribute("image", product.thumbnail?.url?.let { openGraphImage(it) })
        model.addAttribute("type", "website")
    }

    /**
     * Generate open-graph image following commons specification. See https://kaydee.net/blog/open-graph-image
     *  - Aspect ration: 16:9
     *  - Dimension: 1200x630
     */
    private fun openGraphImage(url: String): String =
        imageService.transform(
            url = url,
            transformation = Transformation(
                dimension = Dimension(height = 630)
            )
        )

    private fun findProduct(id: Long): Product =
        catalogApi.getProduct(id).product
}

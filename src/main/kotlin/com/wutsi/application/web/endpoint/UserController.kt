package com.wutsi.application.web.endpoint

import com.wutsi.application.web.Page
import com.wutsi.application.web.model.PageModel
import com.wutsi.marketplace.manager.dto.ProductSummary
import com.wutsi.marketplace.manager.dto.SearchProductRequest
import com.wutsi.membership.manager.dto.Member
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/u")
class UserController : AbstractController() {
    @GetMapping("/{id}")
    fun index(@PathVariable id: Long, model: Model): String {
        val member = findMember(id)
        val country = regulationEngine.country(member.country)

        model.addAttribute("page", createPage(member))
        model.addAttribute("member", mapper.toMemberModel(member))
        model.addAttribute(
            "products",
            findProducts(member).map {
                mapper.toProductModel(it, country)
            }
        )

        return "user"
    }

    private fun createPage(member: Member) = PageModel(
        name = Page.PROFILE,
        title = member.displayName,
        description = member.biography,
        imageUrl = member.pictureUrl,
        assetUrl = assetUrl,
        canonicalUrl = "$serverUrl/u/${member.id}"
    )

    private fun findProducts(member: Member): List<ProductSummary> {
        if (member.storeId == null) {
            return emptyList()
        }

        return marketplaceManagerApi.searchProduct(
            request = SearchProductRequest(
                storeId = member.storeId,
                limit = regulationEngine.maxProducts(),
                sortBy = "RECOMMENDED",
                status = "PUBLISHED"
            )
        ).products
    }
}

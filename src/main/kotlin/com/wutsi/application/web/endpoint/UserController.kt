package com.wutsi.application.web.endpoint

import com.wutsi.application.web.Page
import com.wutsi.application.web.model.MemberModel
import com.wutsi.application.web.model.PageModel
import com.wutsi.enums.ProductSort
import com.wutsi.enums.ProductStatus
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
        val memberModel = mapper.toMemberModel(member)

        model.addAttribute("page", createPage(memberModel))
        model.addAttribute("member", memberModel)
        model.addAttribute(
            "products",
            findProducts(member).map {
                mapper.toProductModel(it, country, member)
            }
        )

        return "user"
    }

    private fun createPage(member: MemberModel) = PageModel(
        name = Page.PROFILE,
        title = member.displayName,
        description = member.biography,
        url = "$serverUrl/${member.url}",
        imageUrl = null,
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
                sortBy = ProductSort.RECOMMENDED.name,
                status = ProductStatus.PUBLISHED.name
            )
        ).products
    }
}

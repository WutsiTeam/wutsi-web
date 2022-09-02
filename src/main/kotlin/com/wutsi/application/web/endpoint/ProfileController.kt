package com.wutsi.application.web.endpoint

import com.wutsi.platform.account.WutsiAccountApi
import com.wutsi.platform.account.dto.Account
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/profile")
class ProfileController(
    private val accountApi: WutsiAccountApi,
) : AbstractPageController() {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(ProfileController::class.java)
    }

    override fun pageId() = "page.profile"

    @GetMapping
    fun index(@RequestParam id: Long, model: Model): String {
        try {
            val account = findAccount(id)
            model.addAttribute("downloadText", "Install the App to view the user's profile")
            addOpenGraph(account, model)
        } catch (ex: Exception) {
            LOGGER.warn("Unexpected error while loading Account$id", ex)
        } finally {
            return "default"
        }
    }

    private fun addOpenGraph(account: Account, model: Model) {
        model.addAttribute("title", account.displayName)
        model.addAttribute("description", account.biography)
        model.addAttribute("image", "/qr-code/${account.id}.png")
        model.addAttribute("site_name", account.website)
        model.addAttribute("type", "profile")
    }

    private fun findAccount(id: Long): Account =
        accountApi.getAccount(id).account
}

package com.wutsi.application.web.endpoint

import com.wutsi.application.news.downstream.blog.dto.StoryDto
import com.wutsi.application.web.downstream.blog.client.WutsiBlogApi
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
@RequestMapping("/story")
class StoryController(
    private val blogApi: WutsiBlogApi,
    private val imageService: ImageService
) : AbstractPageController() {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(StoryController::class.java)
    }

    override fun pageId() = "page.story.read"

    @GetMapping("/read")
    fun index(@RequestParam id: Long, model: Model): String {
        try {
            val story = findStory(id)
            model.addAttribute("downloadText", "Install the App to read the article")
            addOpenGraph(story, model)
        } catch (ex: Exception) {
            LOGGER.warn("Unexpected error while loading Order#$id", ex)
        } finally {
            return "default"
        }
    }

    private fun addOpenGraph(story: StoryDto, model: Model) {
        model.addAttribute("title", story.title)
        model.addAttribute("description", story.summary)
        model.addAttribute("image", story.thumbnailUrl?.let { openGraphImage(it) })
        model.addAttribute("type", "article")
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

    private fun findStory(id: Long): StoryDto =
        blogApi.getStory(id).story
}

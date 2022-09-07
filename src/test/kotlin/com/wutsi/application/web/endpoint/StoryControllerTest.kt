package com.wutsi.application.web.endpoint

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.news.downstream.blog.dto.GetStoryResponse
import com.wutsi.application.news.downstream.blog.dto.StoryDto
import com.wutsi.application.web.downstream.blog.client.WutsiBlogApi
import org.junit.jupiter.api.Test
import org.springframework.boot.test.mock.mockito.MockBean

internal class StoryControllerTest : SeleniumTestSupport() {
    @MockBean
    private lateinit var blogApi: WutsiBlogApi

    @Test
    fun read() {
        // GIVEN
        val story = createStory()
        doReturn(GetStoryResponse(story)).whenever(blogApi).getStory(any())

        // WHEN
        val link = url("story/read?id=${story.id}")
        navigate(link)

        // THEN
        assertCurrentPageIs("page.story.read")

        // OpenGraph
        assertElementAttribute("head title", "text", "${story.title} | Wutsi")
        assertElementAttribute("head meta[name='description']", "content", story.summary)
        assertElementAttribute("head meta[property='og:title']", "content", story.title)
        assertElementAttribute("head meta[property='og:description']", "content", story.summary)
        assertElementAttribute("head meta[property='og:type']", "content", "article")
        assertElementAttribute(
            "head meta[property='og:image']",
            "content",
            story.thumbnailUrl
        )

        assertAppStoreLinksPresent()
    }

    private fun createStory() = StoryDto(
        id = 1,
        title = "39 Carat Neckless",
        summary = "Steal her heart!",
        thumbnailUrl = "https://www.volusion.com/blog/content/images/2021/07/Product-Photos.jpg"
    )
}

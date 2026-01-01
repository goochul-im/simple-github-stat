package github.githubstats

import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(StatsController::class)
class StatsControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var statsService: StatsService

    @MockitoBean
    private lateinit var svgGenerator: SvgGenerator

    @Test
    fun `통계 요청 시 SVG를 반환해야 한다`() {
        // given
        val username = "testuser"
        val stats = GithubStatsDto("Test User", 10, 20, 5, 5, 2, emptyList())
        val svgContent = "<svg>...</svg>"

        // Updated signature match
        `when`(statsService.getStats(username, emptySet(), false, emptySet())).thenReturn(stats)
        `when`(svgGenerator.generateSvg(stats)).thenReturn(svgContent)

        // when & then
        mockMvc.perform(get("/api/stats").param("username", username))
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "image/svg+xml"))
            .andExpect(content().string(svgContent))
    }

    @Test
    fun `유저를 찾을 수 없을 때 404를 반환해야 한다`() {
        // given
        val username = "unknown"
        `when`(statsService.getStats(username, emptySet(), false, emptySet())).thenThrow(IllegalArgumentException("User not found"))

        // when & then
        mockMvc.perform(get("/api/stats").param("username", username))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `특수문자가 포함된 언어(C++) 필터링이 정상 동작해야 한다`() {
        // given
        val username = "testuser"
        val stats = GithubStatsDto("Test User", 10, 20, 5, 5, 2, emptyList())
        // In URL, "c++" might come in as "c  " (two spaces)
        val hideParam = "c  " 
        val expectedHiddenSet = setOf("c++")

        `when`(statsService.getStats(username, emptySet(), false, expectedHiddenSet)).thenReturn(stats)
        `when`(svgGenerator.generateSvg(stats)).thenReturn("<svg></svg>")

        // when & then
        mockMvc.perform(get("/api/stats")
            .param("username", username)
            .param("hide", hideParam))
            .andExpect(status().isOk)
    }
}

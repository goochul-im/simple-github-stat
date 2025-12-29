package github.githubstats

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.client.RestClient

@SpringBootTest
@AutoConfigureMockMvc
class StatsIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var mockServer: MockRestServiceServer

    @TestConfiguration
    class TestConfig {
        @Bean
        fun mockServerAndClient(builder: RestClient.Builder): Pair<MockRestServiceServer, RestClient> {
            val server = MockRestServiceServer.bindTo(builder).build()
            val client = builder
                .baseUrl("https://api.github.com")
                .build()
            return server to client
        }

        @Bean
        fun mockRestServiceServer(pair: Pair<MockRestServiceServer, RestClient>): MockRestServiceServer {
            return pair.first
        }

        @Bean
        @Primary
        fun testGithubRestClient(pair: Pair<MockRestServiceServer, RestClient>): RestClient {
            return pair.second
        }
    }

    @Test
    fun `전체 흐름 통합 테스트`() {
        // given
        val username = "testuser"
        val userResponse = """{"login": "testuser", "name": "Test User"}"""
        val reposResponse = """[{"stargazers_count": 10, "fork": false}]"""
        val issuesResponse = """{"total_count": 3}"""
        val prsResponse = """{"total_count": 5}"""
        val commitsResponse = """{"total_count": 100}"""

        mockServer.expect(requestTo("https://api.github.com/users/$username"))
            .andRespond(withSuccess(userResponse, MediaType.APPLICATION_JSON))
            
        mockServer.expect(requestTo("https://api.github.com/users/$username/repos?per_page=100&type=owner"))
            .andRespond(withSuccess(reposResponse, MediaType.APPLICATION_JSON))

        mockServer.expect(requestTo("https://api.github.com/search/issues?q=type%3Aissue%20author%3A$username"))
            .andRespond(withSuccess(issuesResponse, MediaType.APPLICATION_JSON))

        mockServer.expect(requestTo("https://api.github.com/search/issues?q=type%3Apr%20author%3A$username"))
            .andRespond(withSuccess(prsResponse, MediaType.APPLICATION_JSON))

        mockServer.expect(requestTo("https://api.github.com/search/commits?q=author%3A$username"))
            .andRespond(withSuccess(commitsResponse, MediaType.APPLICATION_JSON))

        // when & then
        val result = mockMvc.perform(get("/api/stats").param("username", username))
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "image/svg+xml"))
            .andReturn()

        val svgContent = result.response.contentAsString
        assertThat(svgContent).contains("Test User&apos;s GitHub Stats")
        assertThat(svgContent).contains("Total Stars:")
        assertThat(svgContent).contains("10")
        assertThat(svgContent).contains("Total Commits:")
        assertThat(svgContent).contains("100")
        assertThat(svgContent).contains("Total PRs:")
        assertThat(svgContent).contains("5")
        assertThat(svgContent).contains("Total Issues:")
        assertThat(svgContent).contains("3")
    }
}

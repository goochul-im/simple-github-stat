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
        val graphqlResponse = """
            {
              "data": {
                "user": {
                  "name": "Test User",
                  "login": "testuser",
                  "repositories": {
                    "nodes": [
                      {
                        "name": "repo1",
                        "isFork": false,
                        "stargazerCount": 10,
                        "languages": {
                          "edges": [
                            {
                              "size": 1000,
                              "node": {
                                "name": "Kotlin",
                                "color": "#A97BFF"
                              }
                            }
                          ]
                        }
                      }
                    ]
                  }
                }
              }
            }
        """.trimIndent()
        
        val issuesResponse = """{"total_count": 3}"""
        val prsResponse = """{"total_count": 5}"""
        val commitsResponse = """{"total_count": 100}"""
        
        // 1. GraphQL User Info
        mockServer.expect(requestTo("https://api.github.com/graphql"))
            .andRespond(withSuccess(graphqlResponse, MediaType.APPLICATION_JSON))

        // 2. Search Stats
        mockServer.expect(requestTo("https://api.github.com/search/issues?q=type%3Aissue%20author%3A$username"))
            .andRespond(withSuccess(issuesResponse, MediaType.APPLICATION_JSON))

        mockServer.expect(requestTo("https://api.github.com/search/issues?q=type%3Apr%20author%3A$username"))
            .andRespond(withSuccess(prsResponse, MediaType.APPLICATION_JSON))

        mockServer.expect(requestTo("https://api.github.com/search/commits?q=author%3A$username"))
            .andRespond(withSuccess(commitsResponse, MediaType.APPLICATION_JSON))

        // Last Month Commits (Use regex to handle dynamic dates and URL encoding)
        // Matches: ...q=author:testuser author-date:YYYY-MM-DD..YYYY-MM-DD
        mockServer.expect(requestTo(org.hamcrest.Matchers.matchesPattern(".*q=author%3A$username.*author-date.*")))
            .andRespond(withSuccess("""{"total_count": 25}""", MediaType.APPLICATION_JSON))

        // No more event fetching calls

        // when & then
        val result = mockMvc.perform(get("/api/stats").param("username", username))
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "image/svg+xml"))
            .andReturn()

        val svgContent = result.response.contentAsString
        assertThat(svgContent).contains("Test User&apos;s GitHub Stats")
        assertThat(svgContent).contains("Total Stars:")
        assertThat(svgContent).contains("10")
        assertThat(svgContent).contains("Last 30 days:")
        assertThat(svgContent).contains("25")
        assertThat(svgContent).contains("Most Used Languages")
        assertThat(svgContent).contains("Kotlin")
    }
}
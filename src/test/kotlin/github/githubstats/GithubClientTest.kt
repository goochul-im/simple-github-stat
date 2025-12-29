package github.githubstats

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess

@RestClientTest(GithubClient::class)
@Import(AppConfig::class)
class GithubClientTest {

    @Autowired
    private lateinit var client: GithubClient

    @Autowired
    private lateinit var server: MockRestServiceServer

    @Test
    fun `GraphQL로 유저와 리포지토리 정보를 가져와야 한다`() {
        val username = "testuser"
        val response = """
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

        server.expect(requestTo("https://api.github.com/graphql"))
            .andRespond(withSuccess(response, MediaType.APPLICATION_JSON))

        val result = client.fetchUserAndReposGraphQL(username)
        assertThat(result?.name).isEqualTo("Test User")
        assertThat(result?.repositories?.nodes).hasSize(1)
        assertThat(result?.repositories?.nodes?.get(0)?.languages?.edges).hasSize(1)
        assertThat(result?.repositories?.nodes?.get(0)?.languages?.edges?.get(0)?.node?.color).isEqualTo("#A97BFF")
    }
}
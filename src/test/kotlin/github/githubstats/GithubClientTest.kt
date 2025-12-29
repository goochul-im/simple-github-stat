package github.githubstats

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess

import org.springframework.context.annotation.Import

@RestClientTest(GithubClient::class)
@Import(AppConfig::class)
class GithubClientTest {

    @Autowired
    private lateinit var client: GithubClient

    @Autowired
    private lateinit var server: MockRestServiceServer

    @Test
    fun `유저 정보를 가져와야 한다`() {
        val username = "testuser"
        val response = """{"login": "testuser", "name": "Test User"}"""
        
        server.expect(requestTo("https://api.github.com/users/$username"))
            .andRespond(withSuccess(response, MediaType.APPLICATION_JSON))

        val result = client.fetchUser(username)
        assertThat(result?.name).isEqualTo("Test User")
    }

    @Test
    fun `레포지토리 목록을 가져와야 한다`() {
        val username = "testuser"
        val response = """[{"stargazers_count": 10, "fork": false}, {"stargazers_count": 5, "fork": true}]"""
        
        server.expect(requestTo("https://api.github.com/users/$username/repos?per_page=100&type=owner"))
            .andRespond(withSuccess(response, MediaType.APPLICATION_JSON))

        val result = client.fetchRepositories(username)
        assertThat(result).hasSize(2)
        assertThat(result[0].stargazers_count).isEqualTo(10)
    }
}

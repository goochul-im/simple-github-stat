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
        val response = """[
            {"name": "repo1", "owner": {"login": "testuser"}, "stargazers_count": 10, "fork": false, "language": "Kotlin"},
            {"name": "repo2", "owner": {"login": "testuser"}, "stargazers_count": 5, "fork": true, "language": null}
        ]"""
        
        server.expect(requestTo("https://api.github.com/users/$username/repos?per_page=100&type=all"))
            .andRespond(withSuccess(response, MediaType.APPLICATION_JSON))

        val result = client.fetchRepositories(username)
        assertThat(result).hasSize(2)
        assertThat(result[0].stargazers_count).isEqualTo(10)
        assertThat(result[0].owner.login).isEqualTo("testuser")
    }

    @Test
    fun `레포지토리 언어 정보를 가져와야 한다`() {
        val owner = "testuser"
        val repo = "repo1"
        val response = """{"Kotlin": 1000, "Java": 500}"""

        server.expect(requestTo("https://api.github.com/repos/$owner/$repo/languages"))
            .andRespond(withSuccess(response, MediaType.APPLICATION_JSON))

        val result = client.fetchRepoLanguages(owner, repo)
        assertThat(result["Kotlin"]).isEqualTo(1000L)
        assertThat(result["Java"]).isEqualTo(500L)
    }
}

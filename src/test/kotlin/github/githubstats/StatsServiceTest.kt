package github.githubstats

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class StatsServiceTest {

    @Mock
    private lateinit var githubClient: GithubClient

    @InjectMocks
    private lateinit var statsService: StatsService

    @Test
    fun `유저 통계를 정확히 계산해야 한다`() {
        // given
        val username = "testuser"
        val repo1 = GraphqlRepository(
            name = "repo1",
            isFork = false,
            stargazerCount = 10,
            languages = GraphqlLanguageConnection(
                listOf(GraphqlLanguageEdge(1000, GraphqlLanguageNode("Kotlin", "#A97BFF")))
            )
        )
        val user = GraphqlUser(
            name = "Test User",
            login = "testuser",
            repositories = GraphqlRepoConnection(listOf(repo1))
        )
        
        `when`(githubClient.fetchUserAndReposGraphQL(username, false)).thenReturn(user)
        `when`(githubClient.searchIssues("type:issue author:$username")).thenReturn(3)
        `when`(githubClient.searchIssues("type:pr author:$username")).thenReturn(5)
        `when`(githubClient.searchCommits("author:$username")).thenReturn(100)

        // when
        val result = statsService.getStats(username)

        // then
        assertThat(result.name).isEqualTo("Test User")
        assertThat(result.totalStars).isEqualTo(10)
        assertThat(result.totalCommits).isEqualTo(100)
        assertThat(result.totalPRs).isEqualTo(5)
        assertThat(result.totalIssues).isEqualTo(3)
        
        assertThat(result.languages).hasSize(1)
        assertThat(result.languages[0].name).isEqualTo("Kotlin")
        assertThat(result.languages[0].color).isEqualTo("#A97BFF")
    }

    @Test
    fun `특정 리포지토리를 제외하고 통계를 계산해야 한다`() {
        // given
        val username = "testuser"
        val repo1 = GraphqlRepository(
            name = "repo1",
            isFork = false,
            stargazerCount = 10,
            languages = GraphqlLanguageConnection(
                listOf(GraphqlLanguageEdge(1000, GraphqlLanguageNode("Kotlin", "#A97BFF")))
            )
        )
        val repo2 = GraphqlRepository(
            name = "ignored-repo",
            isFork = false,
            stargazerCount = 50,
            languages = GraphqlLanguageConnection(
                listOf(GraphqlLanguageEdge(5000, GraphqlLanguageNode("Java", "#B07219")))
            )
        )
        val user = GraphqlUser(
            name = "Test User",
            login = "testuser",
            repositories = GraphqlRepoConnection(listOf(repo1, repo2))
        )
        
        `when`(githubClient.fetchUserAndReposGraphQL(username, false)).thenReturn(user)
        `when`(githubClient.searchIssues("type:issue author:$username")).thenReturn(0)
        `when`(githubClient.searchIssues("type:pr author:$username")).thenReturn(0)
        `when`(githubClient.searchCommits("author:$username")).thenReturn(0)

        // when
        val result = statsService.getStats(username, setOf("ignored-repo"))

        // then
        assertThat(result.totalStars).isEqualTo(10) // 50 stars from ignored repo should be excluded
        assertThat(result.languages).hasSize(1)
        assertThat(result.languages[0].name).isEqualTo("Kotlin")
    }
}
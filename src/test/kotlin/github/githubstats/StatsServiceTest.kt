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
        val user = RestUserResponse("testuser", "Test User")
        val repos = listOf(RestRepository(10, false), RestRepository(20, false))
        
        `when`(githubClient.fetchUser(username)).thenReturn(user)
        `when`(githubClient.fetchRepositories(username)).thenReturn(repos)
        `when`(githubClient.searchIssues("type:issue author:$username")).thenReturn(3)
        `when`(githubClient.searchIssues("type:pr author:$username")).thenReturn(5)
        `when`(githubClient.searchCommits("author:$username")).thenReturn(100)

        // when
        val result = statsService.getStats(username)

        // then
        assertThat(result.name).isEqualTo("Test User")
        assertThat(result.totalStars).isEqualTo(30)
        assertThat(result.totalCommits).isEqualTo(100)
        assertThat(result.totalPRs).isEqualTo(5)
        assertThat(result.totalIssues).isEqualTo(3)
    }
}

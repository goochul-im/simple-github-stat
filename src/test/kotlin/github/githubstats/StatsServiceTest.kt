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
    private lateinit var rawStatsFetcher: RawStatsFetcher

    @InjectMocks
    private lateinit var statsService: StatsService

    @Test
    fun `GraphQL 바이트 기준으로 언어 통계를 계산해야 한다`() {
        // given
        val username = "testuser"
        val repo1 = GraphqlRepository(
            name = "repo1",
            isFork = false,
            stargazerCount = 10,
            languages = GraphqlLanguageConnection(
                listOf(
                    GraphqlLanguageEdge(1000, GraphqlLanguageNode("Kotlin", "#A97BFF")),
                    GraphqlLanguageEdge(500, GraphqlLanguageNode("Java", "#B07219"))
                )
            )
        )

        val rawStats = RawGithubStats(
            name = "Test User",
            repos = listOf(repo1),
            totalIssues = 10,
            totalPRs = 5,
            totalCommits = 100,
            lastMonthCommits = 25
        )

        `when`(rawStatsFetcher.fetch(username, false)).thenReturn(rawStats)

        // when
        val result = statsService.getStats(username)

        // then
        assertThat(result.totalCommits).isEqualTo(100)
        assertThat(result.lastMonthCommits).isEqualTo(25)

        // Total bytes = 1500
        // Kotlin: 1000/1500 = 66.6%
        // Java: 500/1500 = 33.3%

        val kotlinStat = result.languages.find { it.name == "Kotlin" }
        val javaStat = result.languages.find { it.name == "Java" }

        assertThat(kotlinStat).isNotNull
        assertThat(javaStat).isNotNull

        // Assert percentage ranges
        assertThat(kotlinStat?.percentage).isGreaterThan(60.0)
        assertThat(javaStat?.percentage).isLessThan(40.0)
    }

    @Test
    fun `숨김 처리된 언어는 통계에서 제외되어야 한다`() {
        // given
        val username = "testuser"
        val repo1 = GraphqlRepository(
            name = "repo1",
            isFork = false,
            stargazerCount = 0,
            languages = GraphqlLanguageConnection(
                listOf(
                    GraphqlLanguageEdge(1000, GraphqlLanguageNode("Kotlin", "#A97BFF")),
                    GraphqlLanguageEdge(5000, GraphqlLanguageNode("HTML", "#E34C26")) // This should be hidden
                )
            )
        )

        val rawStats = RawGithubStats(
            name = "Test User",
            repos = listOf(repo1),
            totalIssues = 0,
            totalPRs = 0,
            totalCommits = 0,
            lastMonthCommits = 0
        )

        `when`(rawStatsFetcher.fetch(username, false)).thenReturn(rawStats)

        // when
        // Hide "html" (case-insensitive check needed)
        val result = statsService.getStats(username, hiddenLanguages = setOf("html"))

        // then
        // HTML is hidden, so only Kotlin remains.
        assertThat(result.languages).hasSize(1)
        assertThat(result.languages[0].name).isEqualTo("Kotlin")
        assertThat(result.languages[0].percentage).isEqualTo(100.0)
    }
}

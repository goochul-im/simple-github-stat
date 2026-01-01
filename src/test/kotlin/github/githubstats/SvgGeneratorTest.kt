package github.githubstats

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class SvgGeneratorTest {

    @Autowired
    private lateinit var svgGenerator: SvgGenerator

    @Test
    fun `통계 정보를 바탕으로 SVG를 생성해야 한다`() {
        // given
        val stats = GithubStatsDto(
            name = "Test User",
            totalStars = 100,
            totalCommits = 500,
            lastMonthCommits = 50,
            totalPRs = 20,
            totalIssues = 10,
            languages = listOf(
                LanguageStat("Kotlin", 60.0, "60%", "#A97BFF", 0.0, 0.0),
                LanguageStat("Java", 40.0, "40%", "#B07219", 0.0, 0.0)
            )
        )

        // when
        val svg = svgGenerator.generateSvg(stats)

        // then
        assertThat(svg).contains("Test User&apos;s GitHub Stats")
        assertThat(svg).contains("Total Stars:")
        assertThat(svg).contains("100")
        assertThat(svg).contains("Total Commits:")
        assertThat(svg).contains("500")
        assertThat(svg).contains("Last Month:") // New label check
        assertThat(svg).contains("50")          // Last Month Value check
        assertThat(svg).contains("Total PRs:")
        assertThat(svg).contains("20")
        assertThat(svg).contains("Total Issues:")
        assertThat(svg).contains("10")
        assertThat(svg).contains("Most Used Languages")
        assertThat(svg).contains("Kotlin")
        assertThat(svg).contains("60%")
        assertThat(svg).contains("Java")
    }
}

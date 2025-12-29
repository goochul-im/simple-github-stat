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
    fun `SVG가 올바르게 생성되어야 한다`() {
        // given
        val stats = GithubStatsDto(
            name = "Test User",
            totalStars = 100,
            totalCommits = 200,
            totalPRs = 50,
            totalIssues = 10
        )

        // when
        val svg = svgGenerator.generateSvg(stats)

        // then
        assertThat(svg).contains("Test User&apos;s GitHub Stats")
        assertThat(svg).contains("Total Stars:")
        assertThat(svg).contains("100")
        assertThat(svg).contains("Total Commits:")
        assertThat(svg).contains("200")
        assertThat(svg).contains("Total PRs:")
        assertThat(svg).contains("50")
        assertThat(svg).contains("Total Issues:")
        assertThat(svg).contains("10")
    }
}

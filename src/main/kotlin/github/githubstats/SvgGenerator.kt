package github.githubstats

import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context

@Service
class SvgGenerator(private val templateEngine: TemplateEngine) {

    fun generateSvg(stats: GithubStatsDto): String {
        val context = Context()
        context.setVariable("name", stats.name)
        context.setVariable("totalStars", stats.totalStars)
        context.setVariable("totalCommits", stats.totalCommits)
        context.setVariable("lastMonthCommits", stats.lastMonthCommits)
        context.setVariable("totalPRs", stats.totalPRs)
        context.setVariable("totalIssues", stats.totalIssues)
        context.setVariable("languages", stats.languages)

        return templateEngine.process("stats", context)
    }
}

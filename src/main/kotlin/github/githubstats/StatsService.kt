package github.githubstats

import org.springframework.stereotype.Service
import kotlin.math.roundToInt

@Service
class StatsService(private val rawStatsFetcher: RawStatsFetcher) {

    private val defaultColor = "#EDEDED"

    fun getStats(
        username: String,
        excludedRepos: Set<String> = emptySet(),
        includeOrgs: Boolean = false,
        hiddenLanguages: Set<String> = emptySet()
    ): GithubStatsDto {
        val raw = rawStatsFetcher.fetch(username, includeOrgs)

        // Filter out excluded repositories
        val allRepos = raw.repos.filter { !excludedRepos.contains(it.name) }
        val totalStars = allRepos.sumOf { it.stargazerCount }

        // Language Analysis (Byte-based using GraphQL data)
        val sourceRepos = allRepos.filter { !it.isFork }

        val languageBytes = mutableMapOf<String, Long>()
        val languageColorMap = mutableMapOf<String, String>()

        sourceRepos.forEach { repo ->
            repo.languages.edges.forEach { edge ->
                val langName = edge.node.name
                val bytes = edge.size
                val color = edge.node.color ?: defaultColor

                // Skip hidden languages (case-insensitive check)
                if (!hiddenLanguages.contains(langName.lowercase())) {
                    languageBytes[langName] = languageBytes.getOrDefault(langName, 0L) + bytes
                    if (!languageColorMap.containsKey(langName)) {
                        languageColorMap[langName] = color
                    }
                }
            }
        }

        val totalBytes = languageBytes.values.sum()

        // Calculate percentages
        val circumference = 314.159
        var currentOffset = 0.0

        val languages = languageBytes.entries
            .sortedByDescending { it.value }
            .take(5)
            .map { (lang, bytes) ->
                val percentage = if (totalBytes > 0) (bytes.toDouble() / totalBytes) * 100 else 0.0
                val dashArray = (circumference * percentage) / 100
                val stat = LanguageStat(
                    name = lang,
                    percentage = percentage,
                    formattedPercentage = "${percentage.roundToInt()}%",
                    color = languageColorMap[lang] ?: defaultColor,
                    dashArray = dashArray,
                    dashOffset = -currentOffset
                )
                currentOffset += dashArray
                stat
            }

        val topLangsBytes = languages.sumOf { languageBytes[it.name] ?: 0 }
        val otherBytes = totalBytes - topLangsBytes

        val finalLanguages = if (otherBytes > 0) {
            val percentage = (otherBytes.toDouble() / totalBytes) * 100
            val dashArray = (circumference * percentage) / 100
            val otherStat = LanguageStat(
                name = "Other",
                percentage = percentage,
                formattedPercentage = "${percentage.roundToInt()}%",
                color = defaultColor,
                dashArray = dashArray,
                dashOffset = -currentOffset
            )
            languages + otherStat
        } else {
            languages
        }

        return GithubStatsDto(
            name = raw.name,
            totalStars = totalStars,
            totalCommits = raw.totalCommits,
            lastMonthCommits = raw.lastMonthCommits,
            totalPRs = raw.totalPRs,
            totalIssues = raw.totalIssues,
            languages = finalLanguages
        )
    }
}

data class GithubStatsDto(
    val name: String,
    val totalStars: Int,
    val totalCommits: Int,
    val lastMonthCommits: Int,
    val totalPRs: Int,
    val totalIssues: Int,
    val languages: List<LanguageStat>
)

data class LanguageStat(
    val name: String,
    val percentage: Double,
    val formattedPercentage: String,
    val color: String,
    val dashArray: Double,
    val dashOffset: Double
)

package github.githubstats

import org.springframework.stereotype.Service
import kotlin.math.roundToInt

@Service
class StatsService(private val githubClient: GithubClient) {

    private val languageColors = mapOf(
        "Kotlin" to "#A97BFF",
        "Java" to "#b07219",
        "Python" to "#3572A5",
        "JavaScript" to "#f1e05a",
        "TypeScript" to "#2b7489",
        "HTML" to "#e34c26",
        "CSS" to "#563d7c",
        "Go" to "#00ADD8",
        "Rust" to "#dea584",
        "C++" to "#f34b7d",
        "C" to "#555555",
        "Swift" to "#ffac45",
        "Shell" to "#89e051",
        "PHP" to "#4F5D95",
        "Ruby" to "#701516",
        "C#" to "#178600"
    )
    private val defaultColor = "#EDEDED"

    fun getStats(username: String): GithubStatsDto {
        val user = githubClient.fetchUser(username)
            ?: throw IllegalArgumentException("User not found")

        val repos = githubClient.fetchRepositories(username)
        val totalStars = repos.sumOf { it.stargazers_count }
        
        val totalIssues = githubClient.searchIssues("type:issue author:$username")
        val totalPRs = githubClient.searchIssues("type:pr author:$username")
        val totalCommits = githubClient.searchCommits("author:$username")

        // Language Analysis (Byte-based)
        val sourceRepos = repos.filter { !it.fork }
        
        // Use parallel stream to fetch languages for each repo concurrently
        val languageBytes = sourceRepos.parallelStream()
            .map { repo -> 
                githubClient.fetchRepoLanguages(repo.owner.login, repo.name)
            }
            .flatMap { it.entries.stream() }
            .collect(java.util.stream.Collectors.groupingBy(
                { it.key }, 
                java.util.stream.Collectors.summingLong { it.value }
            ))
            
        val totalBytes = languageBytes.values.sum()
        
        // Calculate percentages and prepare chart data
        // SVG Circle Radius = 50, Circumference = 2 * PI * 50 ≈ 314.159
        val circumference = 314.159
        var currentOffset = 0.0
        
        val languages = languageBytes.entries
            .sortedByDescending { it.value }
            .take(5) // Top 5 languages
            .map { (lang, bytes) ->
                val percentage = if (totalBytes > 0) (bytes.toDouble() / totalBytes) * 100 else 0.0
                val dashArray = (circumference * percentage) / 100
                val stat = LanguageStat(
                    name = lang,
                    percentage = percentage,
                    formattedPercentage = "${percentage.roundToInt()}%",
                    color = languageColors[lang] ?: defaultColor,
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
            name = user.name ?: user.login,
            totalStars = totalStars,
            totalCommits = totalCommits,
            totalPRs = totalPRs,
            totalIssues = totalIssues,
            languages = finalLanguages
        )
    }
}

data class GithubStatsDto(
    val name: String,
    val totalStars: Int,
    val totalCommits: Int,
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
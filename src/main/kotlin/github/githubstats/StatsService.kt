package github.githubstats

import org.springframework.stereotype.Service
import kotlin.math.roundToInt

import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class StatsService(private val githubClient: GithubClient) {

    private val defaultColor = "#EDEDED"

    @org.springframework.cache.annotation.Cacheable(value = ["githubStats"], key = "#username + '-' + #includeOrgs + '-' + #excludedRepos.toString() + '-' + #hiddenLanguages.toString()")
    fun getStats(
        username: String, 
        excludedRepos: Set<String> = emptySet(), 
        includeOrgs: Boolean = false,
        hiddenLanguages: Set<String> = emptySet()
    ): GithubStatsDto {
        // 1. Fetch User & Repos (GraphQL)
        val user = githubClient.fetchUserAndReposGraphQL(username, includeOrgs)
            ?: throw IllegalArgumentException("User not found")

        // 2. Fetch Search Stats (REST)
        val totalIssues = githubClient.searchIssues("type:issue author:$username")
        val totalPRs = githubClient.searchIssues("type:pr author:$username")
        val totalCommits = githubClient.searchCommits("author:$username")
        
        // Calculate Last Month Commits
        val today = LocalDate.now()
        val lastMonth = today.minusMonths(1)
        val dateFormatter = DateTimeFormatter.ISO_DATE
        val dateQuery = "author-date:${lastMonth.format(dateFormatter)}..${today.format(dateFormatter)}"
        val lastMonthCommits = githubClient.searchCommits("author:$username $dateQuery")
        
        // Filter out excluded repositories
        val allRepos = user.repositories.nodes.filter { !excludedRepos.contains(it.name) }
        val totalStars = allRepos.sumOf { it.stargazerCount }

        // 3. Language Analysis (Byte-based using GraphQL data)
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
            name = user.name ?: user.login,
            totalStars = totalStars,
            totalCommits = totalCommits,
            lastMonthCommits = lastMonthCommits,
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
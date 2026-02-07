package github.githubstats

import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class RawStatsFetcher(private val githubClient: GithubClient) {

    @Cacheable(value = ["githubStats"], key = "#username + '-' + #includeOrgs")
    fun fetch(username: String, includeOrgs: Boolean = false): RawGithubStats {
        val user = githubClient.fetchUserAndReposGraphQL(username, includeOrgs)
            ?: throw IllegalArgumentException("User not found")

        val totalIssues = githubClient.searchIssues("type:issue author:$username")
        val totalPRs = githubClient.searchIssues("type:pr author:$username")
        val totalCommits = githubClient.searchCommits("author:$username")

        val today = LocalDate.now()
        val lastMonth = today.minusMonths(1)
        val dateFormatter = DateTimeFormatter.ISO_DATE
        val dateQuery = "author-date:${lastMonth.format(dateFormatter)}..${today.format(dateFormatter)}"
        val lastMonthCommits = githubClient.searchCommits("author:$username $dateQuery")

        return RawGithubStats(
            name = user.name ?: user.login,
            repos = user.repositories.nodes,
            totalIssues = totalIssues,
            totalPRs = totalPRs,
            totalCommits = totalCommits,
            lastMonthCommits = lastMonthCommits
        )
    }
}

data class RawGithubStats(
    val name: String,
    val repos: List<GraphqlRepository>,
    val totalIssues: Int,
    val totalPRs: Int,
    val totalCommits: Int,
    val lastMonthCommits: Int
)

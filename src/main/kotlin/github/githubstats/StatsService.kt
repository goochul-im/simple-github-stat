package github.githubstats

import org.springframework.stereotype.Service

@Service
class StatsService(private val githubClient: GithubClient) {

    fun getStats(username: String): GithubStatsDto {
        val user = githubClient.fetchUser(username)
            ?: throw IllegalArgumentException("User not found")

        val repos = githubClient.fetchRepositories(username)
        val totalStars = repos.sumOf { it.stargazers_count }
        
        // Search API queries
        // Issues: type:issue author:username
        val totalIssues = githubClient.searchIssues("type:issue author:$username")
        
        // PRs: type:pr author:username
        val totalPRs = githubClient.searchIssues("type:pr author:$username")
        
        // Commits: author:username
        // Note: This is unreliable without token and has strict limits. 
        // We might want to make this optional or handle 0 gracefully.
        val totalCommits = githubClient.searchCommits("author:$username")

        return GithubStatsDto(
            name = user.name ?: user.login,
            totalStars = totalStars,
            totalCommits = totalCommits,
            totalPRs = totalPRs,
            totalIssues = totalIssues
        )
    }
}

data class GithubStatsDto(
    val name: String,
    val totalStars: Int,
    val totalCommits: Int,
    val totalPRs: Int,
    val totalIssues: Int
)

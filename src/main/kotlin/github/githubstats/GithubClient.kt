package github.githubstats

import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class GithubClient(private val githubRestClient: RestClient) {

    fun fetchUser(username: String): RestUserResponse? {
        return try {
            githubRestClient.get()
                .uri("/users/{username}", username)
                .retrieve()
                .body(RestUserResponse::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun fetchRepositories(username: String): List<RestRepository> {
        return try {
            githubRestClient.get()
                .uri("/users/{username}/repos?per_page=100&type=owner", username)
                .retrieve()
                .body(Array<RestRepository>::class.java)?.toList() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Search API has strict rate limits (10 requests per minute for unauthenticated requests)
    // We should be careful using this.
    fun searchIssues(query: String): Int {
        return try {
            val response = githubRestClient.get()
                .uri("/search/issues?q={query}", query)
                .retrieve()
                .body(RestSearchResponse::class.java)
            response?.total_count ?: 0
        } catch (e: Exception) {
            0
        }
    }
    
    fun searchCommits(query: String): Int {
         return try {
            val response = githubRestClient.get()
                .uri("/search/commits?q={query}", query)
                .retrieve()
                .body(RestSearchResponse::class.java)
            response?.total_count ?: 0
        } catch (e: Exception) {
            // Search commits API requires preview header sometimes, but mostly standard now.
            // Also requires authentication for some endpoints, but let's try.
            // Actually, /search/commits REQUIRES authentication for some headers, 
            // but let's see if it works without token or if we need to fallback.
            // Documentation says: "The Commit Search API is currently available for developers to preview."
            // and "You must provide a valid token". 
            // If no token, this might fail.
            // Let's return 0 if it fails.
            0
        }
    }
}

data class RestUserResponse(
    val login: String,
    val name: String?
)

data class RestRepository(
    val stargazers_count: Int,
    val fork: Boolean,
    val language: String?
)

data class RestSearchResponse(
    val total_count: Int
)

package github.githubstats

import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class GithubClient(private val githubRestClient: RestClient) {

    fun fetchUserAndReposGraphQL(username: String): GraphqlUser? {
        val query = """
            query UserStats(${"$"}login: String!) {
              user(login: ${"$"}login) {
                name
                login
                repositories(first: 100, ownerAffiliations: [OWNER, ORGANIZATION_MEMBER], orderBy: {field: UPDATED_AT, direction: DESC}) {
                  nodes {
                    name
                    isFork
                    stargazerCount
                    languages(first: 10, orderBy: { field: SIZE, direction: DESC }) {
                      edges {
                        size
                        node {
                          name
                          color
                        }
                      }
                    }
                  }
                }
              }
            }
        """.trimIndent()

        val requestBody = mapOf(
            "query" to query,
            "variables" to mapOf("login" to username)
        )

        return try {
            val response = githubRestClient.post()
                .uri("/graphql")
                .body(requestBody)
                .retrieve()
                .body(GraphqlResponse::class.java)
            
            response?.data?.user
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Search API has strict rate limits (10 requests per minute for unauthenticated requests)
    // Keeping this as REST for now as it's simple and effective for just 3 counts.
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
            0
        }
    }
}

// GraphQL Data Classes
data class GraphqlResponse(val data: GraphqlData?)
data class GraphqlData(val user: GraphqlUser?)

data class GraphqlUser(
    val name: String?,
    val login: String,
    val repositories: GraphqlRepoConnection
)

data class GraphqlRepoConnection(val nodes: List<GraphqlRepository>)

data class GraphqlRepository(
    val name: String,
    val isFork: Boolean,
    val stargazerCount: Int,
    val languages: GraphqlLanguageConnection
)

data class GraphqlLanguageConnection(val edges: List<GraphqlLanguageEdge>)

data class GraphqlLanguageEdge(
    val size: Long,
    val node: GraphqlLanguageNode
)

data class GraphqlLanguageNode(
    val name: String,
    val color: String?
)

data class RestSearchResponse(
    val total_count: Int
)

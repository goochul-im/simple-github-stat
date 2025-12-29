package github.githubstats

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.web.client.RestClient

@Configuration
class AppConfig {

    @Value("\${github.token:}")
    private lateinit var githubToken: String

    @Bean
    fun githubRestClient(builder: RestClient.Builder): RestClient {
        val clientBuilder = builder
            .baseUrl("https://api.github.com")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            .defaultHeader("X-GitHub-Api-Version", "2022-11-28")

        if (githubToken.isNotBlank()) {
            clientBuilder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer $githubToken")
        }

        return clientBuilder.build()
    }
}

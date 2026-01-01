package github.githubstats

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.RestClient

@Configuration
class AppConfig {

    @Value("\${github.token:}")
    private lateinit var githubToken: String

    @Bean
    fun objectMapper(): ObjectMapper {
        return ObjectMapper()
            .registerKotlinModule()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    @Bean
    fun githubRestClient(builder: RestClient.Builder, objectMapper: ObjectMapper): RestClient {
        val clientBuilder = builder
            .baseUrl("https://api.github.com")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
            .messageConverters { converters ->
                converters.add(0, MappingJackson2HttpMessageConverter(objectMapper))
            }

        if (githubToken.isNotBlank()) {
            clientBuilder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer $githubToken")
        }

        return clientBuilder.build()
    }
}

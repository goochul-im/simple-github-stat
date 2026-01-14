package github.githubstats

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class CacheWarmupScheduler(
    private val statsService: StatsService,
    @Value("\${cache.warmup.usernames:}") private val usernames: String,
    @Value("\${cache.warmup.include-orgs:false}") private val includeOrgs: Boolean
) {
    private val log = LoggerFactory.getLogger(CacheWarmupScheduler::class.java)

    @EventListener(ApplicationReadyEvent::class)
    fun warmupOnStartup() {
        warmupCache()
    }

    @Scheduled(fixedRateString = "\${cache.warmup.interval-ms:39600000}")
    fun scheduledWarmup() {
        warmupCache()
    }

    private fun warmupCache() {
        if (usernames.isBlank()) {
            return
        }

        val usernameList = usernames.split(",").map { it.trim() }.filter { it.isNotEmpty() }

        usernameList.forEach { username ->
            try {
                log.info("Warming up cache for user: {}", username)
                statsService.getStats(username, includeOrgs = includeOrgs)
                log.info("Cache warmed successfully for user: {}", username)
            } catch (e: Exception) {
                log.warn("Failed to warm cache for user: {}, error: {}", username, e.message)
            }
        }
    }
}

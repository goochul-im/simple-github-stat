package github.githubstats

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class StatsController(
    private val statsService: StatsService,
    private val svgGenerator: SvgGenerator
) {

    @GetMapping("/api/stats")
    fun getStats(
        @RequestParam username: String,
        @RequestParam(required = false) exclude: String?,
        @RequestParam(name = "include_orgs", required = false, defaultValue = "false") includeOrgs: Boolean
    ): ResponseEntity<String> {
        return try {
            val excludedRepos = exclude?.split(",")?.map { it.trim() }?.toSet() ?: emptySet()
            val stats = statsService.getStats(username, excludedRepos, includeOrgs)
            val svg = svgGenerator.generateSvg(stats)

            ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/svg+xml")
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=1800")
                .body(svg)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found")
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching stats")
        }
    }
}

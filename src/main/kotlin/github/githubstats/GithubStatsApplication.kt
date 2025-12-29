package github.githubstats

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class GithubStatsApplication

fun main(args: Array<String>) {
    runApplication<GithubStatsApplication>(*args)
}

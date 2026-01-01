package github.githubstats

object LanguageUtils {
    
    data class LanguageInfo(val name: String, val color: String)

    private val extensionMap = mapOf(
        "kt" to LanguageInfo("Kotlin", "#A97BFF"),
        "java" to LanguageInfo("Java", "#B07219"),
        "js" to LanguageInfo("JavaScript", "#F1E05A"),
        "ts" to LanguageInfo("TypeScript", "#2B7489"),
        "py" to LanguageInfo("Python", "#3572A5"),
        "go" to LanguageInfo("Go", "#00ADD8"),
        "rs" to LanguageInfo("Rust", "#DEA584"),
        "c" to LanguageInfo("C", "#555555"),
        "cpp" to LanguageInfo("C++", "#F34B7D"),
        "cs" to LanguageInfo("C#", "#178600"),
        "html" to LanguageInfo("HTML", "#E34C26"),
        "css" to LanguageInfo("CSS", "#563D7C"),
        "scss" to LanguageInfo("SCSS", "#C6538C"),
        "vue" to LanguageInfo("Vue", "#41B883"),
        "jsx" to LanguageInfo("JavaScript", "#F1E05A"),
        "tsx" to LanguageInfo("TypeScript", "#2B7489"),
        "swift" to LanguageInfo("Swift", "#F05138"),
        "rb" to LanguageInfo("Ruby", "#701516"),
        "php" to LanguageInfo("PHP", "#4F5D95"),
        "sh" to LanguageInfo("Shell", "#89E051"),
        "md" to LanguageInfo("Markdown", "#083FA1"),
        "yml" to LanguageInfo("YAML", "#CB171E"),
        "yaml" to LanguageInfo("YAML", "#CB171E"),
        "json" to LanguageInfo("JSON", "#292929"),
        "xml" to LanguageInfo("XML", "#0060AC"),
        "sql" to LanguageInfo("SQL", "#E38C00")
    )

    fun getLanguageFromFilename(filename: String): LanguageInfo? {
        val extension = filename.substringAfterLast('.', "").lowercase()
        return extensionMap[extension]
    }
}

package bassamalim.hidaya.core.helpers

class Searcher<T> {

    /**
     * Performs a fuzzy search on a list of items using a search query and a key selector
     * @param items List of items to search through
     * @param query Search query string
     * @param keySelector Function to extract the searchable string from an item
     * @param threshold Minimum similarity threshold (0.0 to 1.0)
     * @return List of matching items sorted by relevance
     */
    fun fuzzySearch(
        items: List<T>,
        query: String,
        keySelector: (T) -> String,
        limit: Int = Int.MAX_VALUE,
        threshold: Float = 0.0f
    ): List<T> {
        if (query.isEmpty()) return items

        val normalizedQuery = normalizeString(query)

        return items.mapNotNull { item ->
            val key = keySelector(item)
            val normalizedKey = normalizeString(key)
            val similarity = calculateSimilarity(normalizedKey, normalizedQuery)
            if (similarity >= threshold) {
                Pair(item, similarity)
            } else null
        }.sortedByDescending { it.second }
            .take(limit)
            .map { it.first }
    }

    /**
     * Performs a simple contains search on a list of items
     * @param items List of items to search through
     * @param query Search query string
     * @param keySelector Function to extract the searchable string from an item
     * @return List of matching items
     */
    fun containsSearch(
        items: List<T>,
        query: String,
        keySelector: (T) -> String,
        limit: Int = Int.MAX_VALUE
    ): List<T> {
        if (query.isEmpty()) return items.take(limit)

        val normalizedQuery = normalizeString(query)
        val results = mutableListOf<T>()

        for (item in items) {
            val key = keySelector(item)
            val normalizedKey = normalizeString(key)
            if (normalizedKey.contains(normalizedQuery)) {
                results.add(item)
                if (results.size >= limit) break
            }
        }

        return results
    }

    /**
     * Performs a prefix search on a list of items
     * @param items List of items to search through
     * @param query Search query string
     * @param keySelector Function to extract the searchable string from an item
     * @return List of matching items
     */
    fun prefixSearch(
        items: List<T>,
        query: String,
        keySelector: (T) -> String,
        limit: Int = Int.MAX_VALUE
    ): List<T> {
        if (query.isEmpty()) return items.take(limit)

        val normalizedQuery = normalizeString(query)
        val results = mutableListOf<T>()

        for (item in items) {
            val key = keySelector(item)
            val normalizedKey = normalizeString(key)
            if (normalizedKey.startsWith(normalizedQuery)) {
                results.add(item)
                if (results.size >= limit) break
            }
        }

        return results
    }

    /**
     * Calculates string similarity using Levenshtein distance algorithm
     * @return Similarity score between 0.0 and 1.0
     */
    private fun calculateSimilarity(str1: String, str2: String): Float {
        val dp = Array(str1.length + 1) { IntArray(str2.length + 1) }

        // Initialize first row and column
        for (i in 0..str1.length) dp[i][0] = i
        for (j in 0..str2.length) dp[0][j] = j

        // Fill the dp table
        for (i in 1..str1.length) {
            for (j in 1..str2.length) {
                dp[i][j] = if (str1[i - 1] == str2[j - 1]) {
                    dp[i - 1][j - 1]
                } else {
                    minOf(
                        dp[i - 1][j] + 1,     // deletion
                        dp[i][j - 1] + 1,     // insertion
                        dp[i - 1][j - 1] + 1  // substitution
                    )
                }
            }
        }

        val maxLength = maxOf(str1.length, str2.length)
        return if (maxLength == 0) 1f else 1f - (dp[str1.length][str2.length].toFloat() / maxLength)
    }

    fun normalizeString(str: String, trim: Boolean = true): String {
        val normalized = str
            .lowercase()
            .replace("أ", "ا")
            .replace("إ", "ا")
            .replace("آ", "ا")
            .replace("ة", "ه")
            .replace("ؤ", "و")
            .replace("ى", "ي")
            .replace("ئ", "ي")
        if (trim) return normalized.trim()
        return normalized
    }

}
package de.nielsfalk.game.suite.common

/**
 * What the highscore dialog shows: the top entries of one level, how often it was attempted and
 * whether the data came from the server or from the local cache.
 */
data class LevelHighscore(
    val gameName: String,
    val stage: String,
    val level: Int,
    val entries: List<Score> = emptyList(),
    val totalPlays: Long = 0,
    val online: Boolean = false
) {
    /** The best entry of [username], or `null` when the player has no won attempt yet. */
    fun bestOf(username: String): Score? =
        entries.filter { it.username == username }.minWithOrNull(ScoreSummary.highscoreOrder)

    /** The rank of [username] in this list, or `null` when the player is not ranked. */
    fun rankOf(username: String): Int? {
        val index = entries.indexOfFirst { it.username == username }
        return if (index < 0) null else index + 1
    }

    companion object {
        /** Builds the offline fallback from locally cached scores. */
        fun fromLocal(
            scores: List<Score>,
            gameName: String,
            stage: String,
            level: Int
        ): LevelHighscore {
            val summary = ScoreSummary.levelHighscore(scores, gameName, stage, level)
            return LevelHighscore(
                gameName = gameName,
                stage = stage,
                level = level,
                entries = summary.topScores,
                totalPlays = summary.totalPlays,
                online = false
            )
        }

        /** Wraps the server response. */
        fun fromServer(summary: HighscoreSummary, level: Int): LevelHighscore =
            LevelHighscore(
                gameName = summary.gameName,
                stage = summary.stage,
                level = level,
                entries = summary.topScores.sortedWith(ScoreSummary.highscoreOrder),
                totalPlays = summary.totalPlays,
                online = true
            )
    }
}

/** The user summary shown under the game logos. */
data class UserSummaryView(
    val username: String,
    val entries: List<GameStageSummary>,
    val online: Boolean
) {
    fun entry(gameName: String, stage: String): GameStageSummary? =
        entries.firstOrNull { it.gameName == gameName && it.stage == stage }

    companion object {
        /** Builds the offline fallback from locally cached scores. */
        fun fromLocal(scores: List<Score>, username: String): UserSummaryView =
            UserSummaryView(
                username = username,
                entries = ScoreSummary.summarizeUser(username, scores).entries,
                online = false
            )

        /** Wraps the server response. */
        fun fromServer(summary: UserSummary): UserSummaryView =
            UserSummaryView(
                username = summary.username,
                entries = summary.entries,
                online = true
            )
    }
}

package de.nielsfalk.game.suite.common

/**
 * Pure, platform independent aggregations over scores. Every stored score is a win.
 *
 * The server uses these functions for its summary endpoint, the PWA uses them to build the same
 * aggregates from locally cached scores when the server is unreachable.
 */
object ScoreSummary {
    /** Ranks a level highscore: more points first, then faster time. */
    val highscoreOrder: Comparator<Score> =
        compareByDescending<Score> { it.points }.thenBy { it.time }

    /** Whether [score] belongs to a numbered level. Legacy rows with random ids are ignored. */
    fun isNumberedLevel(score: Score): Boolean = score.level != null

    /** Builds a level highscore from any score list. */
    fun levelHighscore(
        scores: List<Score>,
        gameName: String,
        stage: String,
        level: Int,
        limit: Int = 10
    ): HighscoreSummary {
        val levelId = LevelSeed.format(level)
        val all = scores.filter {
            it.gameName == gameName &&
                it.stage == stage &&
                isNumberedLevel(it) &&
                it.level == level
        }
        val ranked = all.sortedWith(highscoreOrder)
        return HighscoreSummary(
            gameName = gameName,
            stage = stage,
            levelId = levelId,
            topScores = ranked.take(limit),
            totalPlays = all.size.toLong(),
            highestScore = ranked.firstOrNull()?.points
        )
    }

    /** Aggregates scores per game and stage, ignoring legacy rows with random ids. */
    fun summarize(scores: List<Score>): List<GameStageSummary> =
        scores.filter { isNumberedLevel(it) }
            .groupBy { it.gameName to it.stage }
            .map { (gameAndStage, grouped) ->
                val (gameName, stage) = gameAndStage
                GameStageSummary(
                    gameName = gameName,
                    stage = stage,
                    levelsPlayed = grouped.mapNotNull { it.level }.distinct().size,
                    bestPoints = grouped.maxOfOrNull { it.points },
                    totalPoints = grouped.sumOf { it.points },
                    highestLevelPlayed = grouped.mapNotNull { it.level }.maxOrNull()
                )
            }
            .sortedWith(compareBy({ it.gameName }, { it.stage }))

    /** Aggregates scores for one user, as returned by the summary endpoint. */
    fun summarizeUser(username: String, scores: List<Score>): UserSummary =
        UserSummary(
            username = username,
            entries = summarize(scores.filter { it.username == username })
        )
}
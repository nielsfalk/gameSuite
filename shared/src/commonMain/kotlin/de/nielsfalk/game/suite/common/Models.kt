package de.nielsfalk.game.suite.common

import kotlinx.serialization.Serializable

interface GameStage {
    val name: String
}

/**
 * One submitted result of one level.
 *
 * [levelId] holds the level number as text, so that legacy rows written with random ids stay readable
 * and can be ignored explicitly. [won] separates cleared levels from lost attempts: highscores only
 * rank won levels, while the user summary counts both.
 */
@Serializable
data class Score(
    val username: String,
    val gameName: String,
    val stage: String,
    val levelId: String,
    val time: Long,
    val points: Long,
    val won: Boolean = true
) {
    /** The level number, or `null` for legacy rows that stored a random id. */
    val level: Int? get() = levelId.toIntOrNull()?.takeIf { it >= LevelSeed.FIRST_LEVEL }
}

@Serializable
data class HighscoreSummary(
    val gameName: String,
    val stage: String,
    val levelId: String,
    val topScores: List<Score> = emptyList(),
    val totalPlays: Long = 0,
    val highestScore: Long? = null
)

/** Aggregated results of one user for one game and stage. */
@Serializable
data class GameStageSummary(
    val gameName: String,
    val stage: String,
    val levelsPlayed: Int = 0,
    val levelsWon: Int = 0,
    val bestPoints: Long? = null,
    val totalPoints: Long = 0,
    val highestLevelPlayed: Int? = null,
    val highestLevelWon: Int? = null
)

/** Aggregated results of one user across all games and stages. */
@Serializable
data class UserSummary(
    val username: String,
    val entries: List<GameStageSummary> = emptyList()
)

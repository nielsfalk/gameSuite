package de.nielsfalk.game.suite.common

import kotlinx.serialization.Serializable

interface GameStage {
    val name: String
}

@Serializable
data class Score(
    val username: String,
    val gameName: String,
    val stage: String,
    val levelId: String,
    val time: Long,
    val points: Long
)

@Serializable
data class HighscoreSummary(
    val gameName: String,
    val stage: String,
    val levelId: String,
    val topScores: List<Score> = emptyList(),
    val totalPlays: Long = 0,
    val highestScore: Long? = null
)

package de.nielsfalk.game.suite.common

import io.kotest.core.spec.style.FreeSpec
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SharedSmokeTest : FreeSpec({
    "Shared module smoke test" - {
        "should serialize and deserialize Score" - {
            data class ScoreRow(
                val username: String,
                val gameName: String,
                val stage: String,
                val levelId: String,
                val time: Long,
                val points: Long
            )

            val rows = listOf(
                ScoreRow("player1", "memory", "easy", "1", 123456789L, 150L),
                ScoreRow("player2", "memory", "hard", "2", 987654321L, -30L)
            )

            rows.forEach { (username, gameName, stage, levelId, time, points) ->
                "score for $username in $gameName/$stage/$levelId round-trips" {
                    val score = Score(
                        username = username,
                        gameName = gameName,
                        stage = stage,
                        levelId = levelId,
                        time = time,
                        points = points
                    )
                    val json = Json.encodeToString(score)
                    val decoded = Json.decodeFromString<Score>(json)
                    assert(decoded == score)
                    assert(decoded.username == username)
                    assert(decoded.points == points)
                }
            }
        }

        "should serialize and deserialize HighscoreSummary" {
            val scores = listOf(
                Score("alice", "memory", "easy", "1", 1000L, 200L),
                Score("bob", "memory", "easy", "1", 2000L, 150L)
            )
            val summary = HighscoreSummary(
                gameName = "memory",
                stage = "easy",
                levelId = "1",
                topScores = scores,
                totalPlays = 2L,
                highestScore = 200L
            )
            val json = Json.encodeToString(summary)
            val decoded = Json.decodeFromString<HighscoreSummary>(json)
            assert(decoded == summary)
            assert(decoded.topScores.size == 2)
            assert(decoded.highestScore == 200L)
        }

        "should handle empty HighscoreSummary defaults" {
            val summary = HighscoreSummary(
                gameName = "memory",
                stage = "medium",
                levelId = "3"
            )
            assert(summary.topScores.isEmpty())
            assert(summary.totalPlays == 0L)
            assert(summary.highestScore == null)
        }
    }
})
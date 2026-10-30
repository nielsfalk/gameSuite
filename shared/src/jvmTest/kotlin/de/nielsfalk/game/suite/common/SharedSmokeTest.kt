package de.nielsfalk.game.suite.common

import de.nielsfalk.game.suite.common.Game
import de.nielsfalk.game.suite.common.Stage
import io.kotest.core.spec.style.FreeSpec
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SharedSmokeTest : FreeSpec({
    "Shared module smoke test" - {
        "should serialize and deserialize Score" {
            val score = Score(
                username = "player1",
                gameName = "memory",
                stage = "easy",
                levelId = "1",
                time = 123456789L,
                points = 150L
            )
            val json = Json.encodeToString(score)
            val decoded = Json.decodeFromString<Score>(json)
            assert(decoded == score)
            assert(decoded.username == "player1")
            assert(decoded.points == 150L)
        }

        "should evaluate game stages using data table" - {
            data class StageTestData(val stageName: String, val expectedStage: Stage, val expectedMultiplier: Int)

            val stageData = listOf(
                StageTestData("easy", Stage.Easy, 1),
                StageTestData("medium", Stage.Medium, 2),
                StageTestData("hard", Stage.Hard, 3)
            )

            stageData.forEach { (stageName, expectedStage, expectedMultiplier) ->
                "stage '$stageName' maps to $expectedStage with multiplier $expectedMultiplier" {
                    val parsed = Stage.fromString(stageName)
                    assert(parsed == expectedStage)
                    assert(parsed.multiplier == expectedMultiplier)
                    assert(parsed.name == stageName)
                }
            }
        }

        "should calculate signed points properly with data table" - {
            data class ScoreCalculationRow(
                val stage: Stage,
                val isWin: Boolean,
                val moves: Int,
                val timeTaken: Long,
                val expectedPositive: Boolean
            )

            val scoreRows = listOf(
                ScoreCalculationRow(Stage.Easy, true, 4, 10, true),
                ScoreCalculationRow(Stage.Hard, true, 3, 5, true),
                ScoreCalculationRow(Stage.Easy, false, 5, 20, false),
                ScoreCalculationRow(Stage.Hard, false, 9, 50, false)
            )

            scoreRows.forEach { (stage, isWin, moves, timeTaken, expectedPositive) ->
                "scoring for ${stage.name} with win=$isWin moves=$moves" {
                    val game = Game(stage = stage)
                    val points = game.calculatePoints(isWin, moves, timeTaken)
                    if (expectedPositive) {
                        assert(points > 0L)
                    } else {
                        assert(points < 0L)
                    }
                }
            }
        }
    }
})

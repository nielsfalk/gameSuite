package de.nielsfalk.game.suite.common

import de.nielsfalk.game.suite.common.Game
import de.nielsfalk.game.suite.common.Stage
import io.kotest.core.spec.style.FreeSpec
import io.kotest.datatest.withData
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
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
            decoded shouldBe score
        }

        "should evaluate game stages using data-driven testing" - {
            data class StageTestData(val stageName: String, val expectedStage: Stage, val expectedMultiplier: Int)

            withData(
                StageTestData("easy", Stage.Easy, 1),
                StageTestData("medium", Stage.Medium, 2),
                StageTestData("hard", Stage.Hard, 3)
            ) { (stageName, expectedStage, expectedMultiplier) ->
                val parsed = Stage.fromString(stageName)
                parsed shouldBe expectedStage
                parsed.multiplier shouldBe expectedMultiplier
                parsed.name shouldBe stageName
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

            withData(
                ScoreCalculationRow(Stage.Easy, true, 4, 10, true),
                ScoreCalculationRow(Stage.Hard, true, 3, 5, true),
                ScoreCalculationRow(Stage.Easy, false, 5, 20, false),
                ScoreCalculationRow(Stage.Hard, false, 9, 50, false)
            ) { (stage, isWin, moves, timeTaken, expectedPositive) ->
                val game = Game(stage = stage)
                val points = game.calculatePoints(isWin, moves, timeTaken)
                if (expectedPositive) {
                    points shouldBeGreaterThan 0L
                } else {
                    points shouldBeLessThan 0L
                }
            }
        }
    }
})

package de.nielsfalk.game.suite.common

import io.kotest.core.spec.style.FreeSpec

private fun score(
    username: String = "alice",
    gameName: String = "minesweeper",
    stage: String = "easy",
    level: String = "1",
    time: Long = 10,
    points: Long = 100,
    won: Boolean = true
) = Score(
    username = username,
    gameName = gameName,
    stage = stage,
    levelId = level,
    time = time,
    points = points,
    won = won
)

class ScoreSummaryTest : FreeSpec({
    "ScoreSummary" - {
        "orders a level highscore by points descending then time ascending" {
            val summary = ScoreSummary.levelHighscore(
                scores = listOf(
                    score(username = "slow", time = 30, points = 100),
                    score(username = "fast", time = 10, points = 100),
                    score(username = "best", time = 99, points = 250)
                ),
                gameName = "minesweeper",
                stage = "easy",
                level = 1
            )

            assert(summary.topScores.map { it.username } == listOf("best", "fast", "slow"))
            assert(summary.highestScore == 250L)
            assert(summary.totalPlays == 3L)
        }

        "limits the highscore to the requested amount" {
            val summary = ScoreSummary.levelHighscore(
                scores = (1..15).map { score(username = "p$it", points = it.toLong()) },
                gameName = "minesweeper",
                stage = "easy",
                level = 1
            )

            assert(summary.topScores.size == 10)
            assert(summary.topScores.first().username == "p15")
            assert(summary.totalPlays == 15L)
        }

        "excludes lost attempts and other levels, stages and games" {
            val summary = ScoreSummary.levelHighscore(
                scores = listOf(
                    score(username = "winner", points = 10),
                    score(username = "loser", points = 999, won = false),
                    score(username = "wrongLevel", level = "2", points = 500),
                    score(username = "wrongStage", stage = "expert", points = 500),
                    score(username = "wrongGame", gameName = "hexmine", points = 500)
                ),
                gameName = "minesweeper",
                stage = "easy",
                level = 1
            )

            assert(summary.topScores.map { it.username } == listOf("winner"))
            assert(summary.totalPlays == 2L)
            assert(summary.highestScore == 10L)
        }

        "ignores legacy scores with random level ids" {
            val summary = ScoreSummary.levelHighscore(
                scores = listOf(
                    score(username = "legacy", level = "123456789"),
                    score(username = "legacy2", level = "abc"),
                    score(username = "modern", level = "1")
                ),
                gameName = "minesweeper",
                stage = "easy",
                level = 1
            )

            assert(summary.topScores.map { it.username } == listOf("modern"))
            assert(summary.totalPlays == 1L)
        }

        "summarizes per game and stage" {
            val summary = ScoreSummary.summarize(
                listOf(
                    score(level = "1", points = 100),
                    score(level = "2", points = 300),
                    score(level = "2", points = 50, won = false),
                    score(level = "3", points = 500, won = false),
                    score(gameName = "hexmine", stage = "beginner", level = "1", points = 20)
                )
            )

            assert(summary.size == 2)
            val minesweeper = summary.first { it.gameName == "minesweeper" && it.stage == "easy" }
            assert(minesweeper.levelsPlayed == 3)
            assert(minesweeper.levelsWon == 2)
            assert(minesweeper.bestPoints == 300L)
            assert(minesweeper.totalPoints == 950L)
            assert(minesweeper.highestLevelPlayed == 3)
            assert(minesweeper.highestLevelWon == 2)
        }

        "returns empty aggregates without won levels" {
            val summary = ScoreSummary.summarize(listOf(score(won = false)))
            assert(summary.size == 1)
            assert(summary.single().levelsWon == 0)
            assert(summary.single().bestPoints == null)
            assert(summary.single().highestLevelWon == null)
        }

        "summarizes one user and ignores legacy ids" {
            val user = ScoreSummary.summarizeUser(
                username = "alice",
                scores = listOf(
                    score(username = "alice", level = "1", points = 10),
                    score(username = "bob", level = "1", points = 999),
                    score(username = "alice", level = "1234567890123456789", points = 500)
                )
            )

            assert(user.username == "alice")
            assert(user.entries.size == 1)
            assert(user.entries.single().levelsPlayed == 1)
            assert(user.entries.single().bestPoints == 10L)
        }
    }
})

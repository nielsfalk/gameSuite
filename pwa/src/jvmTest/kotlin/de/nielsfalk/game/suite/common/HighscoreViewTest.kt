package de.nielsfalk.game.suite.common

import io.kotest.core.spec.style.FreeSpec

private fun viewScore(
    username: String = "alice",
    gameName: String = "minesweeper",
    stage: String = "easy",
    level: String = "1",
    time: Long = 10,
    points: Long = 100,
    won: Boolean = true
) = Score(username, gameName, stage, level, time, points, won)

class HighscoreViewTest : FreeSpec({
    "ServerConfig" - {
        "defaults to the deployed server" {
            assert(ServerConfig.resolve(null) == ServerConfig.DEFAULT_BASE_URL)
            assert(ServerConfig.DEFAULT_BASE_URL == "https://gamesuite-server.onrender.com")
        }

        "uses a stored override" {
            assert(ServerConfig.resolve("http://localhost:8080") == "http://localhost:8080")
        }

        "trims the override and ignores blank values" {
            assert(ServerConfig.resolve("  http://localhost:8080/  ") == "http://localhost:8080")
            assert(ServerConfig.resolve("") == ServerConfig.DEFAULT_BASE_URL)
            assert(ServerConfig.resolve("   ") == ServerConfig.DEFAULT_BASE_URL)
        }
    }

    "LevelHighscore" - {
        "wraps a server response and reports it as online" {
            val summary = HighscoreSummary(
                gameName = "minesweeper",
                stage = "easy",
                levelId = "3",
                topScores = listOf(
                    viewScore(username = "slow", points = 50, time = 20),
                    viewScore(username = "fast", points = 50, time = 5)
                ),
                totalPlays = 7
            )

            val view = LevelHighscore.fromServer(summary, level = 3)
            assert(view.online)
            assert(view.totalPlays == 7L)
            assert(view.entries.map { it.username } == listOf("fast", "slow"))
            assert(view.rankOf("fast") == 1)
            assert(view.bestOf("slow")?.time == 20L)
        }

        "builds an offline fallback from local scores" {
            val view = LevelHighscore.fromLocal(
                scores = listOf(
                    viewScore(username = "alice", level = "2", points = 40),
                    viewScore(username = "bob", level = "2", points = 90),
                    viewScore(username = "bob", level = "2", points = 100, won = false),
                    viewScore(username = "bob", level = "1", points = 900)
                ),
                gameName = "minesweeper",
                stage = "easy",
                level = 2
            )

            assert(!view.online)
            assert(view.entries.map { it.username } == listOf("bob", "alice"))
            assert(view.totalPlays == 3L)
            assert(view.rankOf("alice") == 2)
            assert(view.rankOf("nobody") == null)
        }
    }

    "UserSummaryView" - {
        "wraps a server response and reports it as online" {
            val view = UserSummaryView.fromServer(
                UserSummary(
                    username = "alice",
                    entries = listOf(
                        GameStageSummary(gameName = "minesweeper", stage = "easy", levelsWon = 2)
                    )
                )
            )

            assert(view.online)
            assert(view.entry("minesweeper", "easy")?.levelsWon == 2)
            assert(view.entry("minesweeper", "expert") == null)
        }

        "builds an offline fallback from local scores" {
            val view = UserSummaryView.fromLocal(
                scores = listOf(
                    viewScore(username = "alice", level = "1", points = 10),
                    viewScore(username = "alice", level = "2", points = 20),
                    viewScore(username = "bob", level = "1", points = 30)
                ),
                username = "alice"
            )

            assert(!view.online)
            val entry = view.entry("minesweeper", "easy")!!
            assert(entry.levelsPlayed == 2)
            assert(entry.levelsWon == 2)
            assert(entry.bestPoints == 20L)
            assert(entry.totalPoints == 30L)
        }
    }
})

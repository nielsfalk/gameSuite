package de.nielsfalk.game.suite.common

import io.kotest.core.spec.style.FreeSpec

class HighscoreViewTest : FreeSpec({
    "LevelHighscore" - {
        "wraps a server response and reports it as online" {
            val summary = HighscoreSummary(
                gameName = "minesweeper",
                stage = "easy",
                levelId = "3",
                topScores = listOf(
                    Score("slow", "minesweeper", "easy", "3", 20, 50),
                    Score("fast", "minesweeper", "easy", "3", 5, 50)
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
                    Score("alice", "minesweeper", "easy", "2", 10, 40),
                    Score("bob", "minesweeper", "easy", "2", 20, 90),
                    Score("bob", "minesweeper", "easy", "1", 5, 900)
                ),
                gameName = "minesweeper",
                stage = "easy",
                level = 2
            )

            assert(!view.online)
            assert(view.entries.map { it.username } == listOf("bob", "alice"))
            assert(view.totalPlays == 2L)
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
                        GameStageSummary(gameName = "minesweeper", stage = "easy", levelsPlayed = 2)
                    )
                )
            )

            assert(view.online)
            assert(view.entry("minesweeper", "easy")?.levelsPlayed == 2)
            assert(view.entry("minesweeper", "expert") == null)
        }

        "builds an offline fallback from local scores" {
            val view = UserSummaryView.fromLocal(
                scores = listOf(
                    Score("alice", "minesweeper", "easy", "1", 5, 10),
                    Score("alice", "minesweeper", "easy", "2", 8, 20),
                    Score("bob", "minesweeper", "easy", "1", 3, 30)
                ),
                username = "alice"
            )

            assert(!view.online)
            val entry = view.entry("minesweeper", "easy")!!
            assert(entry.levelsPlayed == 2)
            assert(entry.totalPoints == 30L)
        }
    }
})
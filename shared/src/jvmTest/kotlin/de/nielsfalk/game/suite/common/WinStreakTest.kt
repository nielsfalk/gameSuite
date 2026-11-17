package de.nielsfalk.game.suite.common

import io.kotest.core.spec.style.FreeSpec

class WinStreakTest : FreeSpec({
    "WinStreak" - {
        "unlocks once after three consecutive wins" {
            val streak = WinStreak()

            assert(!streak.recordWin())
            assert(!streak.recordWin())
            assert(streak.recordWin())
            assert(streak.consecutiveWins == 3)
            assert(streak.stageUpUnlocked)
        }

        "does not unlock a second time in the same stage" {
            val streak = WinStreak()
            repeat(3) { streak.recordWin() }

            repeat(5) {
                assert(!streak.recordWin())
            }
            assert(streak.stageUpUnlocked)
        }

        "resets the streak on a loss" {
            val streak = WinStreak()
            streak.recordWin()
            streak.recordWin()
            streak.recordLoss()

            assert(streak.consecutiveWins == 0)
            assert(streak.winsMissing == 3)
            assert(!streak.recordWin())
        }

        "resets the streak on a restart" {
            val streak = WinStreak()
            streak.recordWin()
            streak.recordWin()
            streak.restart()

            assert(streak.consecutiveWins == 0)
            assert(!streak.recordWin())
        }

        "counts down the remaining wins" {
            val streak = WinStreak(winsRequired = 4)
            assert(streak.winsMissing == 4)
            streak.recordWin()
            assert(streak.winsMissing == 3)
        }

        "requires at least one win" {
            val streak = WinStreak(winsRequired = 1)
            assert(streak.recordWin())
            assert(streak.stageUpUnlocked)
        }
    }
})

package de.nielsfalk.game.suite.common

import io.kotest.core.spec.style.FreeSpec

class GameCatalogTest : FreeSpec({
    "GameCatalog" - {
        "knows the supported games" {
            assert(GameCatalog.isValidGame(GameCatalog.MINESWEEPER))
            assert(GameCatalog.isValidGame("minesweeper"))
            assert(!GameCatalog.isValidGame("unknown"))
            assert(!GameCatalog.isValidGame(""))
        }

        "accepts every preset stage for minesweeper" - {
            listOf("easy", "beginner", "expert").forEach { stage ->
                "stage $stage" {
                    assert(GameCatalog.isValidStage(GameCatalog.MINESWEEPER, stage))
                    assert(GameCatalog.isValid(GameCatalog.MINESWEEPER, stage))
                }
            }
        }

        "accepts valid custom minesweeper stages" - {
            listOf(
                "custom-5x5-1",
                "custom-12x10-20",
                "custom-30x30-100"
            ).forEach { stage ->
                "stage $stage" {
                    assert(GameCatalog.isValidStage(GameCatalog.MINESWEEPER, stage))
                }
            }
        }

        "rejects unknown or malformed minesweeper stages" - {
            listOf(
                "",
                "hard",
                "custom",
                "custom-4x4-1",
                "custom-12x10-1000",
                "custom-12x10"
            ).forEach { stage ->
                "stage $stage" {
                    assert(!GameCatalog.isValidStage(GameCatalog.MINESWEEPER, stage))
                }
            }
        }

        "rejects stages for unknown games" {
            assert(!GameCatalog.isValidStage("unknown", "easy"))
            assert(!GameCatalog.isValid("unknown", "easy"))
        }
    }
})

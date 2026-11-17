package de.nielsfalk.game.suite.common

import io.kotest.core.spec.style.FreeSpec

class GameCatalogTest : FreeSpec({
    "GameCatalog" - {
        "knows the supported games" {
            assert(GameCatalog.isValidGame(GameCatalog.MINESWEEPER))
            assert(GameCatalog.isValidGame("minesweeper"))
            assert(GameCatalog.isValidGame(GameCatalog.HEXMINE))
            assert(GameCatalog.isValidGame("hexmine"))
            assert(GameCatalog.isValidGame(GameCatalog.HEXLASER))
            assert(GameCatalog.isValidGame("hexlaser"))
            assert(!GameCatalog.isValidGame("unknown"))
            assert(!GameCatalog.isValidGame(""))
        }

        "exposes one game entry per supported game" {
            assert(GameCatalog.games.map { it.name } == listOf("minesweeper", "hexmine", "hexlaser"))
            GameCatalog.games.forEach { game ->
                assert(GameCatalog.game(game.name) == game)
                assert(game.displayName.isNotBlank())
                assert(game.tagline.isNotBlank())
                assert(game.defaultStage in game.stages)
            }
        }

        "accepts every preset stage for minesweeper" - {
            listOf("easy", "beginner", "expert").forEach { stage ->
                "stage $stage" {
                    assert(GameCatalog.isValidStage(GameCatalog.MINESWEEPER, stage))
                    assert(GameCatalog.isValid(GameCatalog.MINESWEEPER, stage))
                }
            }
        }

        "accepts the same preset stages for hexmine" - {
            listOf("easy", "beginner", "expert").forEach { stage ->
                "stage $stage" {
                    assert(GameCatalog.isValidStage(GameCatalog.HEXMINE, stage))
                    assert(GameCatalog.isValidStage(GameCatalog.HEXMINE, stage))
                }
            }
        }

        "rejects unknown, malformed or legacy custom stages" - {
            listOf(
                GameCatalog.MINESWEEPER,
                GameCatalog.HEXMINE
            ).forEach { gameName ->
                listOf(
                    "",
                    "hard",
                    "custom",
                    "custom-5x5-1",
                    "custom-12x10-20",
                    "custom-30x30-100",
                    "custom-4x4-1",
                    "custom-12x10-1000",
                    "custom-12x10"
                ).forEach { stage ->
                    "$gameName stage '$stage'" {
                        assert(!GameCatalog.isValidStage(gameName, stage))
                    }
                }
            }
        }

        "accepts every hexlaser stage" - {
            listOf(
                "absolute_beginner",
                "easy",
                "intermediate",
                "intermediate_plus",
                "intermediate_plus2",
                "hard",
                "harder",
                "insane",
                "nightmare",
                "nightmare_plus",
                "nightmare_plus2"
            ).forEach { stage ->
                "stage $stage" {
                    assert(GameCatalog.isValidStage(GameCatalog.HEXLASER, stage))
                    assert(GameCatalog.isValid(GameCatalog.HEXLASER, stage))
                }
            }
        }

        "accepts hexlaser stages regardless of case" {
            assert(GameCatalog.isValidStage(GameCatalog.HEXLASER, "HARD"))
        }

        "rejects unknown or malformed hexlaser stages" - {
            listOf("", "impossible", "easy-1").forEach { stage ->
                "stage $stage" {
                    assert(!GameCatalog.isValidStage(GameCatalog.HEXLASER, stage))
                }
            }
        }

        "rejects stages for unknown games" {
            assert(!GameCatalog.isValidStage("unknown", "easy"))
            assert(!GameCatalog.isValid("unknown", "easy"))
        }

        "orders the stages from easiest to hardest" - {
            data class OrderCase(val gameName: String, val defaultStage: String, val lastStage: String)

            listOf(
                OrderCase("minesweeper", "easy", "expert"),
                OrderCase("hexmine", "easy", "expert"),
                OrderCase("hexlaser", "absolute_beginner", "nightmare_plus2")
            ).forEach { (gameName, defaultStage, lastStage) ->
                "$gameName" {
                    val game = GameCatalog.game(gameName)!!
                    assert(game.defaultStage == defaultStage)
                    assert(game.stages.last() == lastStage)
                    assert(game.isLastStage(lastStage))
                    assert(!game.isLastStage(defaultStage))
                    assert(game.nextStage(lastStage) == null)
                }
            }
        }

        "walks every stage up to the last stage without wrapping around" - {
            GameCatalog.games.forEach { game ->
                game.name {
                    var stage = game.defaultStage
                    val visited = mutableListOf(stage)
                    while (!game.isLastStage(stage)) {
                        stage = game.nextStage(stage) ?: break
                        assert(stage !in visited)
                        visited.add(stage)
                    }
                    assert(visited == game.stages)
                }
            }
        }
    }
})

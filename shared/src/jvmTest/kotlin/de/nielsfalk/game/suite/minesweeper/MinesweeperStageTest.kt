package de.nielsfalk.game.suite.minesweeper

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MinesweeperStageTest : FreeSpec({
    "MinesweeperStage" - {
        "presets have the expected dimensions and mine counts" - {
            data class PresetCase(
                val stage: MinesweeperStage,
                val name: String,
                val width: Int,
                val height: Int,
                val mines: Int
            )

            val cases = listOf(
                PresetCase(MinesweeperStage.Easy, "easy", 9, 9, 10),
                PresetCase(MinesweeperStage.Beginner, "beginner", 16, 16, 40),
                PresetCase(MinesweeperStage.Expert, "expert", 16, 30, 99)
            )

            cases.forEach { (stage, name, width, height, mines) ->
                "$name preset" {
                    assert(stage.name == name)
                    assert(stage.width == width)
                    assert(stage.height == height)
                    assert(stage.mines == mines)
                    assert(stage.cells == width * height)
                }
            }
        }

        "parses every preset from its name" - {
            MinesweeperStage.presets.forEach { stage ->
                "parse ${stage.name}" {
                    assert(MinesweeperStage.parse(stage.name) == stage)
                }
            }
        }

        "parses custom stages from their encoded name" - {
            val stage = MinesweeperStage.Custom(width = 12, height = 10, mines = 20)

            assert(stage.name == "custom-12x10-20")
            assert(MinesweeperStage.parse("custom-12x10-20") == stage)
        }

        "returns null for unknown or malformed stage names" - {
            listOf(
                "",
                "unknown",
                "custom",
                "custom-12x10",
                "custom-12x10-20-30",
                "custom-ax10-20",
                "custom-0x10-20",
                "custom-12x10-0",
                "custom-12x10-1000"
            ).forEach { encoded ->
                "parse '$encoded'" {
                    assert(MinesweeperStage.parse(encoded) == null)
                }
            }
        }

        "rejects custom stages outside the allowed bounds" {
            shouldThrow<IllegalArgumentException> {
                MinesweeperStage.Custom(width = 4, height = 10, mines = 5)
            }
            shouldThrow<IllegalArgumentException> {
                MinesweeperStage.Custom(width = 31, height = 10, mines = 5)
            }
            shouldThrow<IllegalArgumentException> {
                MinesweeperStage.Custom(width = 10, height = 4, mines = 5)
            }
            shouldThrow<IllegalArgumentException> {
                MinesweeperStage.Custom(width = 10, height = 10, mines = 0)
            }
            shouldThrow<IllegalArgumentException> {
                MinesweeperStage.Custom(width = 5, height = 5, mines = 17)
            }
        }

        "accepts the maximum safe mine count for a custom stage" {
            val stage = MinesweeperStage.Custom(width = 5, height = 5, mines = 16)

            assert(stage.cells - MinesweeperStage.SAFE_FIRST_CLICK_CELLS == stage.mines)
        }

        "serializes presets and custom stages as a sealed hierarchy" - {
            val stages = MinesweeperStage.presets + MinesweeperStage.Custom(width = 7, height = 8, mines = 9)

            stages.forEach { stage ->
                "round-trip ${stage.name}" {
                    val json = Json.encodeToString<MinesweeperStage>(stage)
                    val decoded = Json.decodeFromString<MinesweeperStage>(json)
                    assert(decoded == stage)
                }
            }
        }
    }
})

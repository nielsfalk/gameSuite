package de.nielsfalk.game.suite.minesweeper

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

        "returns null for unknown or legacy stage names" - {
            listOf(
                "",
                "unknown",
                "hard",
                "custom",
                "custom-12x10-20",
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

        "serializes the presets as a sealed hierarchy" - {
            MinesweeperStage.presets.forEach { stage ->
                "round-trip ${stage.name}" {
                    val json = Json.encodeToString<MinesweeperStage>(stage)
                    val decoded = Json.decodeFromString<MinesweeperStage>(json)
                    assert(decoded == stage)
                }
            }
        }
    }
})

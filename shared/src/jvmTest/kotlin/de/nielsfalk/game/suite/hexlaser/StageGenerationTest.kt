package de.nielsfalk.game.suite.hexlaser

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe

class StageGenerationTest : FreeSpec({
    "same stage, seed, and orientation produce the same grid" {
        val first = generateHexlaserGrid(StageType.EASY, seed = 12345L)
        val second = generateHexlaserGrid(StageType.EASY, seed = 12345L)

        second shouldBe first
    }

    "toggleXY swaps the generated dimensions" {
        val normal = generateHexlaserGrid(StageType.EASY, seed = 12345L, toggleXY = false)
        val toggled = generateHexlaserGrid(StageType.EASY, seed = 12345L, toggleXY = true)

        toggled.x shouldBe normal.y
        toggled.y shouldBe normal.x
    }

    "stage type wire names parse back to stages" {
        StageType.entries.forEach { stageType ->
            StageType.parse(stageType.wireName) shouldBe stageType
        }
        StageType.parse("not-a-stage") shouldBe null
    }
})

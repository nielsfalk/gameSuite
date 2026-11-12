package de.nielsfalk.game.suite.hexlaser

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlin.math.max

class HexlasarGameTest : FreeSpec({
    "new game is generated from the seed" {
        val game = HexlasarGame(StageType.EASY, seed = 12345L)

        game.grid shouldBe generateHexlaserGrid(StageType.EASY, seed = 12345L)
        game.solved shouldBe false
        game.hintCount shouldBe 0
        game.points shouldBe game.requiredRotations.toLong()
    }

    "rotating until solved locks the grid and counts one solve" {
        val game = HexlasarGame(StageType.EASY, seed = 12345L)

        game.solve()

        game.solved shouldBe true
        game.solvedAndLocked shouldBe true
        game.solvingCount[StageType.EASY] shouldBe 1
    }

    "hints lock a cell and reduce the score" {
        val game = HexlasarGame(StageType.EASY, seed = 12345L)
        val pointsBeforeHint = game.points

        val hint = game.requestHint()

        hint shouldNotBe null
        game.hintCount shouldBe 1
        game.grid[hint!!.position].locked shouldBe true
        game.points shouldBe max(
            0L,
            pointsBeforeHint - hint.rotations - HexlasarGame.HINT_PENALTY
        )
    }

    "retry restores the generated grid" {
        val game = HexlasarGame(StageType.EASY, seed = 12345L)
        game.requestHint()

        game.retry()

        game.hintCount shouldBe 0
        game.grid shouldBe generateHexlaserGrid(StageType.EASY, seed = 12345L)
    }

    "stage up is offered after three solved grids" {
        val game = HexlasarGame(StageType.EASY, seed = 12345L)

        repeat(HexlasarGame.SOLVES_TO_STAGE_UP) { index ->
            game.solve()
            game.solvingCount[StageType.EASY] shouldBe index + 1
            if (index < HexlasarGame.SOLVES_TO_STAGE_UP - 1) {
                game.nextGrid(newSeed = 12345L + index + 1)
            }
        }

        game.stageUpVisible shouldBe true
    }

    "next stage advances the stage type and keeps the seed" {
        val game = HexlasarGame(StageType.EASY, seed = 12345L)

        game.nextStage(newSeed = 99L)

        game.stageType shouldBe StageType.EASY.next()
        game.seed shouldBe 99L
        game.grid shouldBe generateHexlaserGrid(StageType.EASY.next(), seed = 99L)
    }
})

private fun HexlasarGame.solve() {
    while (!solved) {
        val cell = grid.cells.firstOrNull {
            !it.locked &&
                    it.connections.isNotEmpty() &&
                    it.connections.size != Direction.entries.size &&
                    it.rotations % it.connections.scrambleAmount != 0 &&
                    it.rotatedParts == 0
        } ?: break
        rotate(cell.position, cell.pendingRotations)
    }
}

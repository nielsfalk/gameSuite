package de.nielsfalk.game.suite.minesweeper

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec

private fun MinesweeperGame.mineCoordinates(): Set<Pair<Int, Int>> {
    val mines = mutableSetOf<Pair<Int, Int>>()
    grid.forEachCell { x, y, cell -> if (cell.hasMine) mines.add(x to y) }
    return mines
}

private fun MinesweeperGame.coordinates(): List<Pair<Int, Int>> = buildList {
    grid.forEachCell { x, y, _ -> add(x to y) }
}

class MinesweeperGameTest : FreeSpec({
    "MinesweeperGame" - {
        "places the configured number of mines after the first reveal" {
            val game = MinesweeperGame(MinesweeperStage.Easy, seed = 42L)

            assert(!game.minesPlaced)
            val result = game.reveal(4, 4)

            assert(result != RevealResult.Lost)
            assert(game.minesPlaced)
            assert(game.mineCoordinates().size == MinesweeperStage.Easy.mines)
        }

        "keeps the first revealed cell and its neighbors free of mines" - {
            listOf(
                42L,
                7L,
                1234L
            ).forEach { seed ->
                "seed $seed" {
                    val game = MinesweeperGame(MinesweeperStage.Custom(9, 9, 10), seed = seed)
                    game.reveal(4, 4)

                    assert(!game.cellAt(4, 4).hasMine)
                    game.grid.neighbors(4, 4).forEach { (x, y) ->
                        assert(!game.cellAt(x, y).hasMine)
                    }
                }
            }
        }

        "reproduces the same board for the same seed and first click" {
            val stage = MinesweeperStage.Custom(9, 9, 10)
            val first = MinesweeperGame(stage, seed = 99L)
            val second = MinesweeperGame(stage, seed = 99L)

            first.reveal(2, 3)
            second.reveal(2, 3)

            assert(first.mineCoordinates() == second.mineCoordinates())
        }

        "produces a different board for a different seed" {
            val stage = MinesweeperStage.Custom(9, 9, 10)
            val first = MinesweeperGame(stage, seed = 1L)
            val second = MinesweeperGame(stage, seed = 2L)

            first.reveal(4, 4)
            second.reveal(4, 4)

            assert(first.mineCoordinates() != second.mineCoordinates())
        }

        "flood-fills neighbors of every revealed empty cell" {
            val game = MinesweeperGame(MinesweeperStage.Easy, seed = 42L)
            game.reveal(4, 4)

            game.coordinates().forEach { (x, y) ->
                val cell = game.cellAt(x, y)
                if (cell.revealed && !cell.hasMine && cell.adjacentMines == 0) {
                    game.grid.neighbors(x, y).forEach { (nx, ny) ->
                        assert(game.cellAt(nx, ny).revealed)
                    }
                }
            }
        }

        "ignores repeated reveals without charging again" {
            val game = MinesweeperGame(MinesweeperStage.Easy, seed = 42L)

            game.reveal(4, 4)
            val clicksAfterFirstReveal = game.revealClicks
            val result = game.reveal(4, 4)

            assert(result == RevealResult.Ignored)
            assert(game.revealClicks == clicksAfterFirstReveal)
        }

        "flags hidden cells, charges for each flag click, and blocks revealing them" {
            val game = MinesweeperGame(MinesweeperStage.Easy, seed = 42L)
            game.reveal(4, 4)

            val hidden = game.coordinates().first { (x, y) -> !game.cellAt(x, y).revealed }
            assert(game.toggleFlag(hidden.first, hidden.second))

            assert(game.cellAt(hidden.first, hidden.second).flagged)
            assert(game.flagsPlaced == 1)
            assert(game.flagClicks == 1L)
            assert(game.reveal(hidden.first, hidden.second) == RevealResult.Ignored)
            assert(game.points == -(game.revealClicks * MinesweeperGame.REVEAL_COST + 15L))

            assert(game.toggleFlag(hidden.first, hidden.second))
            assert(!game.cellAt(hidden.first, hidden.second).flagged)
            assert(game.flagClicks == 2L)
        }

        "cannot flag revealed cells" {
            val game = MinesweeperGame(MinesweeperStage.Easy, seed = 42L)
            game.reveal(4, 4)

            assert(!game.toggleFlag(4, 4))
            assert(game.flagClicks == 0L)
        }

        "loses when revealing a mine and reveals all mines" {
            val game = MinesweeperGame(MinesweeperStage.Easy, seed = 42L)
            game.reveal(4, 4)

            val mine = game.mineCoordinates().first()
            val result = game.reveal(mine.first, mine.second)

            assert(result == RevealResult.Lost)
            assert(game.status == MinesweeperStatus.Lost)
            assert(game.cellAt(mine.first, mine.second).revealed)
            game.mineCoordinates().forEach { (x, y) ->
                assert(game.cellAt(x, y).revealed)
            }
            assert(game.reveal(0, 0) == RevealResult.Ignored)
            assert(!game.toggleFlag(0, 0))
        }

        "wins when every safe cell is revealed" {
            val game = MinesweeperGame(MinesweeperStage.Custom(5, 5, 1), seed = 7L)
            game.reveal(2, 2)

            game.coordinates().forEach { (x, y) ->
                if (!game.cellAt(x, y).hasMine) {
                    game.reveal(x, y)
                }
            }

            assert(game.status == MinesweeperStatus.Won)
            assert(game.revealedSafeCells == game.stage.cells - game.stage.mines)
            assert(game.points == -(game.revealClicks * MinesweeperGame.REVEAL_COST + game.flagClicks * MinesweeperGame.FLAG_COST))
        }

        "computes points as negative reveal and flag costs" {
            val game = MinesweeperGame(MinesweeperStage.Custom(9, 9, 10), seed = 5L)
            game.reveal(0, 0)

            val hidden = game.coordinates().first { (x, y) -> !game.cellAt(x, y).revealed }
            game.toggleFlag(hidden.first, hidden.second)

            assert(game.points == -25L)
        }

        "rejects reveals outside the board" {
            val game = MinesweeperGame(MinesweeperStage.Easy, seed = 42L)

            shouldThrow<IllegalArgumentException> { game.reveal(-1, 0) }
            shouldThrow<IllegalArgumentException> { game.reveal(0, 9) }
        }
    }
})

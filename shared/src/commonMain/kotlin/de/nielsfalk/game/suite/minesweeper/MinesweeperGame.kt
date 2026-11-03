package de.nielsfalk.game.suite.minesweeper

import de.nielsfalk.game.suite.common.SquareGrid
import kotlin.random.Random

data class MinesweeperCell(
    val hasMine: Boolean = false,
    val adjacentMines: Int = 0,
    val revealed: Boolean = false,
    val flagged: Boolean = false
)

enum class MinesweeperStatus {
    InProgress,
    Won,
    Lost
}

enum class RevealResult {
    Revealed,
    Won,
    Lost,
    Ignored
}

/**
 * Mutable, seeded Minesweeper board.
 *
 * Mines are placed lazily on the first reveal so that the first revealed cell (and its
 * neighbors) is always safe. The same [seed] and first click therefore reproduce the same
 * board, which is what makes deep-linked levels repeatable.
 */
class MinesweeperGame(
    val stage: MinesweeperStage,
    val seed: Long,
    private val random: Random = Random(seed)
) {
    companion object {
        const val REVEAL_COST: Long = 10L
        const val FLAG_COST: Long = 15L
    }

    val grid: SquareGrid<MinesweeperCell> =
        SquareGrid(stage.width, stage.height) { _, _ -> MinesweeperCell() }

    var status: MinesweeperStatus = MinesweeperStatus.InProgress
        private set

    var minesPlaced: Boolean = false
        private set

    var revealClicks: Long = 0
        private set

    var flagClicks: Long = 0
        private set

    val isOver: Boolean get() = status != MinesweeperStatus.InProgress

    val points: Long get() = -(revealClicks * REVEAL_COST + flagClicks * FLAG_COST)

    val flagsPlaced: Int
        get() {
            var count = 0
            grid.forEachCell { _, _, cell -> if (cell.flagged) count++ }
            return count
        }

    val minesRemaining: Int get() = stage.mines - flagsPlaced

    val revealedSafeCells: Int
        get() {
            var count = 0
            grid.forEachCell { _, _, cell -> if (cell.revealed && !cell.hasMine) count++ }
            return count
        }

    fun cellAt(x: Int, y: Int): MinesweeperCell = grid[x, y]

    fun reveal(x: Int, y: Int): RevealResult {
        if (isOver) return RevealResult.Ignored

        val current = grid[x, y]
        if (current.revealed || current.flagged) return RevealResult.Ignored

        revealClicks++

        if (!minesPlaced) {
            placeMines(safeX = x, safeY = y)
        }

        val cell = grid[x, y]
        if (cell.hasMine) {
            status = MinesweeperStatus.Lost
            revealAllMines()
            return RevealResult.Lost
        }

        revealFrom(x, y)

        if (revealedSafeCells == stage.cells - stage.mines) {
            status = MinesweeperStatus.Won
            flagAllMines()
            return RevealResult.Won
        }

        return RevealResult.Revealed
    }

    fun toggleFlag(x: Int, y: Int): Boolean {
        if (isOver) return false

        val cell = grid[x, y]
        if (cell.revealed) return false

        grid[x, y] = cell.copy(flagged = !cell.flagged)
        flagClicks++
        return true
    }

    private fun placeMines(safeX: Int, safeY: Int) {
        val excluded = (grid.neighbors(safeX, safeY) + (safeX to safeY)).toSet()
        val candidates = buildList {
            grid.forEachCell { x, y, _ -> if ((x to y) !in excluded) add(x to y) }
        }
        require(stage.mines <= candidates.size) {
            "stage has ${stage.mines} mines but only ${candidates.size} safe cells are available"
        }

        val mines = candidates.shuffled(random).take(stage.mines).toSet()
        grid.forEachCell { x, y, cell ->
            if ((x to y) in mines) {
                grid[x, y] = cell.copy(hasMine = true)
            }
        }

        grid.forEachCell { x, y, cell ->
            val adjacent = grid.neighbors(x, y).count { (nx, ny) -> grid[nx, ny].hasMine }
            grid[x, y] = cell.copy(adjacentMines = adjacent)
        }

        minesPlaced = true
    }

    private fun revealFrom(startX: Int, startY: Int) {
        val queue = ArrayDeque<Pair<Int, Int>>()
        queue.addLast(startX to startY)

        while (queue.isNotEmpty()) {
            val (x, y) = queue.removeFirst()
            val cell = grid[x, y]
            if (cell.revealed || cell.flagged || cell.hasMine) continue

            grid[x, y] = cell.copy(revealed = true)

            if (cell.adjacentMines == 0) {
                grid.neighbors(x, y).forEach { (nx, ny) ->
                    val neighbor = grid[nx, ny]
                    if (!neighbor.revealed && !neighbor.flagged && !neighbor.hasMine) {
                        queue.addLast(nx to ny)
                    }
                }
            }
        }
    }

    private fun revealAllMines() {
        grid.forEachCell { x, y, cell ->
            if (cell.hasMine) {
                grid[x, y] = cell.copy(revealed = true)
            }
        }
    }

    private fun flagAllMines() {
        grid.forEachCell { x, y, cell ->
            if (cell.hasMine && !cell.flagged) {
                grid[x, y] = cell.copy(flagged = true)
            }
        }
    }
}

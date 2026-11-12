package de.nielsfalk.game.suite.hexlaser

import kotlin.math.max
import kotlin.random.Random

data class HintStep(
    val position: Position,
    val rotations: Int
)

/**
 * Mutable state of one hexlaser grid, independent of any UI framework.
 *
 * The grid is generated deterministically from [stageType], [seed], and [toggleXY].
 */
class HexlasarGame(
    stageType: StageType = StageType.entries.first(),
    seed: Long = DEFAULT_SEED,
    toggleXY: Boolean = false
) {
    companion object {
        const val HINT_PENALTY: Long = 5L
        const val SOLVES_TO_STAGE_UP: Int = 3
    }

    var stageType: StageType = stageType
        private set

    var seed: Long = seed
        private set

    var toggleXY: Boolean = toggleXY
        private set

    var grid: Grid = generateHexlaserGrid(stageType, seed, toggleXY)
        private set

    var hintCount: Int = 0
        private set

    var solvingCount: Map<StageType, Int> = StageType.entries.associateWith { 0 }
        private set

    var stageUpVisible: Boolean = false
        private set

    var animationSpendTime: Int? = null
        private set

    private var solvedAwarded: Boolean = false

    val performedRotations: Int
        get() = grid.cells.sumOf { it.rotations - it.initialRotation }

    val requiredRotations: Int
        get() = grid.cells.sumOf {
            if (it.initialRotation == 0) 0 else Direction.entries.size - it.initialRotation
        }

    val points: Long
        get() = max(
            0L,
            requiredRotations.toLong() - performedRotations.toLong() - HINT_PENALTY * hintCount
        )

    val solved: Boolean get() = grid.solved

    val solvedAndLocked: Boolean get() = grid.solvedAndLocked

    fun rotate(position: Position, rotations: Int = 1) {
        val cell = grid[position]
        if (cell.locked || rotations == 0) return

        grid = grid.update(
            cell.copy(
                rotations = cell.rotations + rotations,
                rotatedParts = 0
            )
        )
            .removePrismaGlow(position)
            .removeDisconnectedFromPaths()
        afterMove()
    }

    fun setRotatedParts(position: Position, parts: Int) {
        val cell = grid[position]
        if (cell.locked) return
        grid = grid.update(cell.copy(rotatedParts = parts))
    }

    fun lock(position: Position) {
        grid = grid.update(grid[position].toggleLock())
    }

    fun retry() {
        resetGrid(seed)
    }

    fun nextGrid(newSeed: Long = Random.nextLong()) {
        seed = newSeed
        resetGrid(newSeed)
    }

    fun nextStage(newSeed: Long = Random.nextLong()) {
        stageType = stageType.next()
        seed = newSeed
        resetGrid(newSeed)
    }

    fun dismissStageUp() {
        stageUpVisible = false
    }

    fun setToggleXY(value: Boolean) {
        if (toggleXY == value) return
        toggleXY = value
        resetGrid(seed)
    }

    fun setAnimationSpendTime(value: Int?) {
        animationSpendTime = value
    }

    fun requestHint(): HintStep? {
        if (grid.solved) return null

        val random = Random(seed + hintCount)
        val cell = grid.cells.filter {
            it.connections.isNotEmpty() &&
                    it.connections.size != Direction.entries.size &&
                    it.rotations % it.connections.scrambleAmount != 0 &&
                    it.rotatedParts == 0
        }
            .run {
                filter { it.locked }.randomOrNull(random)
                    ?: filter { it.endPoint.isEmpty() }.randomOrNull(random)
                    ?: randomOrNull(random)
            }
            ?: return null

        val rotations = cell.pendingRotations
        if (rotations <= 0) return null

        hintCount++
        grid = grid.update(
            cell.copy(
                locked = true,
                rotations = cell.rotations + rotations,
                rotatedParts = 0
            )
        )
            .removePrismaGlow(cell.position)
            .removeDisconnectedFromPaths()
        afterMove()

        return HintStep(position = cell.position, rotations = rotations)
    }

    fun followGlow(): Boolean {
        val before = grid
        grid = grid.followPath()
        return grid != before
    }

    private fun resetGrid(newSeed: Long) {
        grid = generateHexlaserGrid(stageType, newSeed, toggleXY)
        hintCount = 0
        solvedAwarded = false
        stageUpVisible = false
        animationSpendTime = null
    }

    private fun afterMove() {
        grid = grid.copy(glowPath = GlowPath()).initGlowPath().followPathComplete()
        if (solvedAwarded || !grid.solved) return

        solvedAwarded = true
        grid = grid.update(grid.cells.map { it.copy(locked = true) })
        val solvedForStage = (solvingCount[stageType] ?: 0) + 1
        solvingCount = solvingCount + (stageType to solvedForStage)
        stageUpVisible = solvedForStage == SOLVES_TO_STAGE_UP &&
                stageType != StageType.entries.last()
    }
}

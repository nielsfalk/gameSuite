package de.nielsfalk.game.suite.hexlaser

import kotlin.random.Random

/**
 * Generates a deterministic grid for [stageType] and [seed].
 *
 * The same stage, seed, and orientation always produce the same grid.
 */
fun generateHexlaserGrid(
    stageType: StageType,
    seed: Long = DEFAULT_SEED,
    toggleXY: Boolean = false
): Grid {
    val random = Random(seed)
    val stageProperties = stageType.stageProperties.random(random).let {
        if (toggleXY) it.copy(x = it.y, y = it.x) else it
    }
    return GridGenerator(
        stageType = stageType,
        seed = seed,
        random = random,
        stageProperties = stageProperties
    ).generate().initGlowPath()
}

package de.nielsfalk.game.suite.common

/**
 * A reusable rectangular grid of square cells.
 *
 * Cells are addressed by zero-based [x] (column) and [y] (row) coordinates.
 */
class SquareGrid<T> : AbstractGrid<T> {
    constructor(width: Int, height: Int, initializer: (x: Int, y: Int) -> T) :
        super(width, height, initializer)

    constructor(width: Int, height: Int, initialValue: T) :
        super(width, height, initialValue)

    /**
     * Returns all existing neighbors around [x], [y], including diagonals.
     */
    override fun neighbors(x: Int, y: Int): List<Pair<Int, Int>> {
        require(inBounds(x, y)) { "($x, $y) is outside the ${width}x$height grid" }
        return buildList {
            for (dy in -1..1) {
                for (dx in -1..1) {
                    if (dx == 0 && dy == 0) continue
                    val neighborX = x + dx
                    val neighborY = y + dy
                    if (inBounds(neighborX, neighborY)) add(neighborX to neighborY)
                }
            }
        }
    }
}

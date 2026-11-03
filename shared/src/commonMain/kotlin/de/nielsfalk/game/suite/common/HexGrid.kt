package de.nielsfalk.game.suite.common

/**
 * A rectangular board of pointy-top hexagonal cells using odd-r offset coordinates:
 * odd rows are shifted right by half a hex.
 *
 * Cells are addressed by zero-based [x] (column) and [y] (row) coordinates.
 */
class HexGrid<T> : AbstractGrid<T> {
    constructor(width: Int, height: Int, initializer: (x: Int, y: Int) -> T) :
        super(width, height, initializer)

    constructor(width: Int, height: Int, initialValue: T) :
        super(width, height, initialValue)

    override fun neighbors(x: Int, y: Int): List<Pair<Int, Int>> {
        require(inBounds(x, y)) { "($x, $y) is outside the ${width}x$height grid" }
        val deltas = if (y % 2 == 0) EVEN_ROW_NEIGHBORS else ODD_ROW_NEIGHBORS
        return deltas
            .map { (dx, dy) -> x + dx to y + dy }
            .filter { (neighborX, neighborY) -> inBounds(neighborX, neighborY) }
    }

    private companion object {
        val EVEN_ROW_NEIGHBORS = listOf(
            1 to 0,
            0 to -1,
            -1 to -1,
            -1 to 0,
            -1 to 1,
            0 to 1
        )

        val ODD_ROW_NEIGHBORS = listOf(
            1 to 0,
            1 to -1,
            0 to -1,
            -1 to 0,
            0 to 1,
            1 to 1
        )
    }
}

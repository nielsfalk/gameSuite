package de.nielsfalk.game.suite.common

/**
 * A reusable rectangular grid of square cells.
 *
 * Cells are addressed by zero-based [x] (column) and [y] (row) coordinates.
 */
class SquareGrid<T>(
    val width: Int,
    val height: Int,
    initializer: (x: Int, y: Int) -> T
) {
    init {
        require(width > 0) { "width must be positive, was $width" }
        require(height > 0) { "height must be positive, was $height" }
    }

    private val values: MutableList<T> = MutableList(width * height) { index ->
        initializer(index % width, index / width)
    }

    constructor(width: Int, height: Int, initialValue: T) : this(width, height, { _, _ -> initialValue })

    val size: Int get() = values.size

    operator fun get(x: Int, y: Int): T = values[indexOf(x, y)]

    operator fun set(x: Int, y: Int, value: T) {
        values[indexOf(x, y)] = value
    }

    fun inBounds(x: Int, y: Int): Boolean = x in 0 until width && y in 0 until height

    fun indexOf(x: Int, y: Int): Int {
        require(inBounds(x, y)) { "($x, $y) is outside the ${width}x$height grid" }
        return y * width + x
    }

    /**
     * Returns all existing neighbors around [x], [y], including diagonals.
     */
    fun neighbors(x: Int, y: Int): List<Pair<Int, Int>> {
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

    fun forEachCell(action: (x: Int, y: Int, value: T) -> Unit) {
        for (y in 0 until height) {
            for (x in 0 until width) {
                action(x, y, values[y * width + x])
            }
        }
    }

    fun toList(): List<T> = values.toList()
}

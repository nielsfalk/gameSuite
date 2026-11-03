package de.nielsfalk.game.suite.common

/**
 * A two-dimensional grid of cells addressed by zero-based [x] (column) and [y] (row)
 * coordinates.
 *
 * Implementations define what "neighbor" means for their geometry, e.g. four/eight
 * neighbors for a square grid or six neighbors for a hex grid.
 */
interface Grid<T> {
    val width: Int
    val height: Int

    val size: Int get() = width * height

    operator fun get(x: Int, y: Int): T

    operator fun set(x: Int, y: Int, value: T)

    fun inBounds(x: Int, y: Int): Boolean

    fun indexOf(x: Int, y: Int): Int

    /**
     * Returns all existing neighbors of the cell at [x], [y].
     */
    fun neighbors(x: Int, y: Int): List<Pair<Int, Int>>

    fun forEachCell(action: (x: Int, y: Int, value: T) -> Unit)

    fun toList(): List<T>
}

/**
 * Shared mutable cell storage for rectangular grids.
 *
 * Subclasses only define their geometry via [neighbors].
 */
abstract class AbstractGrid<T>(
    final override val width: Int,
    final override val height: Int,
    initializer: (x: Int, y: Int) -> T
) : Grid<T> {
    init {
        require(width > 0) { "width must be positive, was $width" }
        require(height > 0) { "height must be positive, was $height" }
    }

    protected constructor(width: Int, height: Int, initialValue: T) :
        this(width, height, { _, _ -> initialValue })

    private val values: MutableList<T> = MutableList(width * height) { index ->
        initializer(index % width, index / width)
    }

    final override val size: Int get() = values.size

    final override operator fun get(x: Int, y: Int): T = values[indexOf(x, y)]

    final override operator fun set(x: Int, y: Int, value: T) {
        values[indexOf(x, y)] = value
    }

    final override fun inBounds(x: Int, y: Int): Boolean =
        x in 0 until width && y in 0 until height

    final override fun indexOf(x: Int, y: Int): Int {
        require(inBounds(x, y)) { "($x, $y) is outside the ${width}x$height grid" }
        return y * width + x
    }

    final override fun forEachCell(action: (x: Int, y: Int, value: T) -> Unit) {
        for (y in 0 until height) {
            for (x in 0 until width) {
                action(x, y, values[y * width + x])
            }
        }
    }

    final override fun toList(): List<T> = values.toList()
}

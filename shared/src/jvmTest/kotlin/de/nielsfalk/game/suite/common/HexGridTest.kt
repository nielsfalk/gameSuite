package de.nielsfalk.game.suite.common

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec

class HexGridTest : FreeSpec({
    "HexGrid" - {
        "implements the Grid interface" {
            val grid: Grid<String> = HexGrid(width = 3, height = 3, initialValue = "empty")

            assert(grid.width == 3)
            assert(grid.height == 3)
            assert(grid.size == 9)
            assert(grid[1, 2] == "empty")
            assert(grid.neighbors(1, 1).size == 6)

            grid[1, 2] = "changed"
            assert(grid[1, 2] == "changed")
        }

        "initializes every cell with its coordinates" {
            val grid = HexGrid(width = 3, height = 2) { x, y -> x * 10 + y }

            assert(grid[0, 0] == 0)
            assert(grid[2, 0] == 20)
            assert(grid[0, 1] == 1)
            assert(grid[2, 1] == 21)
            assert(grid.toList() == listOf(0, 10, 20, 1, 11, 21))
        }

        "returns the six neighbors for inner even and odd rows" - {
            data class NeighborCase(
                val x: Int,
                val y: Int,
                val expected: Set<Pair<Int, Int>>
            )

            val cases = listOf(
                NeighborCase(
                    x = 2,
                    y = 2,
                    expected = setOf(
                        3 to 2,
                        2 to 1,
                        1 to 1,
                        1 to 2,
                        1 to 3,
                        2 to 3
                    )
                ),
                NeighborCase(
                    x = 2,
                    y = 3,
                    expected = setOf(
                        3 to 3,
                        3 to 2,
                        2 to 2,
                        1 to 3,
                        2 to 4,
                        3 to 4
                    )
                )
            )

            cases.forEach { (x, y, expected) ->
                "($x, $y)" {
                    val grid = HexGrid(width = 5, height = 5) { _, _ -> Unit }
                    val neighbors = grid.neighbors(x, y)

                    assert(neighbors.size == 6)
                    assert(neighbors.toSet() == expected)
                    assert(neighbors.toSet().size == neighbors.size)
                }
            }
        }

        "filters neighbors outside the board" - {
            data class EdgeCase(
                val x: Int,
                val y: Int,
                val expected: Set<Pair<Int, Int>>
            )

            val cases = listOf(
                EdgeCase(0, 0, setOf(1 to 0, 0 to 1)),
                EdgeCase(4, 0, setOf(3 to 0, 3 to 1, 4 to 1)),
                EdgeCase(0, 1, setOf(1 to 1, 1 to 0, 0 to 0, 0 to 2, 1 to 2)),
                EdgeCase(4, 4, setOf(4 to 3, 3 to 3, 3 to 4))
            )

            cases.forEach { (x, y, expected) ->
                "($x, $y)" {
                    val grid = HexGrid(width = 5, height = 5) { _, _ -> Unit }

                    assert(grid.neighbors(x, y).toSet() == expected)
                }
            }
        }

        "has symmetric neighbor relationships" {
            val grid = HexGrid(width = 6, height = 5) { x, y -> y * 6 + x }

            grid.forEachCell { x, y, _ ->
                grid.neighbors(x, y).forEach { (neighborX, neighborY) ->
                    assert((x to y) in grid.neighbors(neighborX, neighborY))
                }
            }
        }

        "reports bounds and rejects coordinates outside the grid" {
            val grid = HexGrid(width = 3, height = 2) { _, _ -> Unit }

            assert(grid.inBounds(0, 0))
            assert(grid.inBounds(2, 1))
            assert(!grid.inBounds(-1, 0))
            assert(!grid.inBounds(0, 2))

            shouldThrow<IllegalArgumentException> { grid[-1, 0] }
            shouldThrow<IllegalArgumentException> { grid[0, 2] }
            shouldThrow<IllegalArgumentException> { grid.neighbors(3, 2) }
        }

        "iterates every cell exactly once" {
            val grid = HexGrid(width = 3, height = 2) { x, y -> x * 10 + y }
            val visited = mutableListOf<Pair<Int, Int>>()

            grid.forEachCell { x, y, _ -> visited.add(x to y) }

            assert(visited.size == grid.size)
            assert(visited.toSet().size == grid.size)
        }

        "rejects non-positive dimensions" {
            shouldThrow<IllegalArgumentException> { HexGrid(width = 0, height = 1) { _, _ -> Unit } }
            shouldThrow<IllegalArgumentException> { HexGrid(width = 1, height = 0) { _, _ -> Unit } }
        }
    }
})

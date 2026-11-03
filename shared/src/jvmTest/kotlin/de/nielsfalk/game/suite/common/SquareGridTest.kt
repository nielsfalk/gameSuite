package de.nielsfalk.game.suite.common

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec

class SquareGridTest : FreeSpec({
    "SquareGrid" - {
        "implements the Grid interface" {
            val grid: Grid<Int> = SquareGrid(width = 3, height = 3) { x, y -> x * 10 + y }

            assert(grid.width == 3)
            assert(grid.height == 3)
            assert(grid.size == 9)
            assert(grid[1, 2] == 12)
            assert(grid.neighbors(1, 1).size == 8)

            grid[0, 0] = 99
            assert(grid[0, 0] == 99)
        }

        "initializes every cell with its coordinates" {
            val grid = SquareGrid(width = 3, height = 2) { x, y -> x * 10 + y }

            assert(grid.width == 3)
            assert(grid.height == 2)
            assert(grid.size == 6)
            assert(grid[0, 0] == 0)
            assert(grid[2, 0] == 20)
            assert(grid[0, 1] == 1)
            assert(grid[2, 1] == 21)
        }

        "can be initialized with a constant value" {
            val grid = SquareGrid(width = 2, height = 2, initialValue = "empty")

            assert(grid.toList() == listOf("empty", "empty", "empty", "empty"))
        }

        "supports indexed reads and writes" {
            val grid = SquareGrid(width = 4, height = 3) { _, _ -> 0 }

            grid[1, 2] = 42

            assert(grid[1, 2] == 42)
            assert(grid[0, 0] == 0)
        }

        "reports whether coordinates are inside the grid" - {
            data class BoundsCase(val x: Int, val y: Int, val expected: Boolean)

            val cases = listOf(
                BoundsCase(0, 0, true),
                BoundsCase(2, 1, true),
                BoundsCase(-1, 0, false),
                BoundsCase(0, -1, false),
                BoundsCase(3, 0, false),
                BoundsCase(0, 2, false)
            )

            cases.forEach { (x, y, expected) ->
                "($x, $y) in 3x2 is $expected" {
                    val grid = SquareGrid(width = 3, height = 2) { _, _ -> Unit }
                    assert(grid.inBounds(x, y) == expected)
                }
            }
        }

        "returns all existing neighbors including diagonals" - {
            data class NeighborCase(val x: Int, val y: Int, val expectedCount: Int)

            val cases = listOf(
                NeighborCase(1, 1, 8),
                NeighborCase(0, 0, 3),
                NeighborCase(2, 2, 3),
                NeighborCase(1, 0, 5),
                NeighborCase(0, 1, 5)
            )

            cases.forEach { (x, y, expectedCount) ->
                "($x, $y) in 3x3 has $expectedCount neighbors" {
                    val grid = SquareGrid(width = 3, height = 3) { _, _ -> Unit }
                    val neighbors = grid.neighbors(x, y)

                    assert(neighbors.size == expectedCount)
                    assert(neighbors.all { (nx, ny) -> grid.inBounds(nx, ny) })
                    assert((x to y) !in neighbors)
                }
            }
        }

        "iterates every cell exactly once" {
            val grid = SquareGrid(width = 3, height = 2) { x, y -> x * 10 + y }
            val visited = mutableListOf<Pair<Int, Int>>()

            grid.forEachCell { x, y, _ -> visited.add(x to y) }

            assert(visited.size == grid.size)
            assert(visited.toSet().size == grid.size)
        }

        "rejects coordinates outside the grid" {
            val grid = SquareGrid(width = 2, height = 2) { _, _ -> 0 }

            shouldThrow<IllegalArgumentException> { grid[-1, 0] }
            shouldThrow<IllegalArgumentException> { grid[0, 2] }
            shouldThrow<IllegalArgumentException> { grid.neighbors(2, 2) }
        }

        "rejects non-positive dimensions" {
            shouldThrow<IllegalArgumentException> { SquareGrid(width = 0, height = 1) { _, _ -> Unit } }
            shouldThrow<IllegalArgumentException> { SquareGrid(width = 1, height = 0) { _, _ -> Unit } }
        }
    }
})

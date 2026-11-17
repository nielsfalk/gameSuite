package de.nielsfalk.game.suite.common

import io.kotest.core.spec.style.FreeSpec

class LevelSeedTest : FreeSpec({
    "LevelSeed" - {
        "derives the same seed for the same game, stage and level" - {
            data class SeedCase(
                val gameName: String,
                val stage: String,
                val level: Int
            )

            val cases = listOf(
                SeedCase("minesweeper", "easy", 1),
                SeedCase("minesweeper", "expert", 42),
                SeedCase("hexmine", "beginner", 7),
                SeedCase("hexlaser", "nightmare_plus2", 999)
            )

            cases.forEach { (gameName, stage, level) ->
                "$gameName/$stage/$level" {
                    val first = LevelSeed.of(gameName, stage, level)
                    val second = LevelSeed.of(gameName, stage, level)
                    assert(first == second)
                    assert(first != 0L)
                }
            }
        }

        "derives different seeds for different levels of the same stage" {
            val seeds = (1..20).map { LevelSeed.of("minesweeper", "easy", it) }
            assert(seeds.toSet().size == seeds.size)
        }

        "derives different seeds for the same level of different stages and games" - {
            data class CollisionCase(val gameName: String, val stage: String)

            val cases = listOf(
                CollisionCase("minesweeper", "easy"),
                CollisionCase("minesweeper", "beginner"),
                CollisionCase("hexmine", "easy"),
                CollisionCase("hexlaser", "easy")
            )

            cases.forEach { (gameName, stage) ->
                "seed for $gameName/$stage level 1" {
                    val seed = LevelSeed.of(gameName, stage, 1)
                    val others = cases
                        .filterNot { it == CollisionCase(gameName, stage) }
                        .map { LevelSeed.of(it.gameName, it.stage, 1) }
                    assert(others.none { it == seed })
                }
            }
        }

        "formats and parses level numbers" - {
            val cases = listOf(
                Triple("1", 1, "1"),
                Triple("2", 2, "2"),
                Triple("123", 123, "123")
            )

            cases.forEach { (encoded, expectedLevel, expectedFormat) ->
                "level '$encoded'" {
                    assert(LevelSeed.parse(encoded) == expectedLevel)
                    assert(LevelSeed.format(expectedLevel) == expectedFormat)
                }
            }
        }

        "falls back to level 1 for legacy random ids and invalid input" - {
            listOf(null, "", "abc", "0", "-5", "1234567890123456789012").forEach { encoded ->
                "parse '$encoded'" {
                    assert(LevelSeed.parse(encoded) == LevelSeed.FIRST_LEVEL)
                }
            }
        }

        "coerces invalid level numbers to the first level when formatting" - {
            listOf(0, -3).forEach { level ->
                "format $level" {
                    assert(LevelSeed.format(level) == "1")
                }
            }
        }

        "rejects level numbers below the first level" {
            val error = runCatching { LevelSeed.of("minesweeper", "easy", 0) }.exceptionOrNull()
            assert(error is IllegalArgumentException)
        }

        "seeds from GameCatalog match the level seeds" {
            assert(GameCatalog.seed("hexmine", "easy", 5) == LevelSeed.of("hexmine", "easy", 5))
        }

        "validates level numbers" {
            listOf("1", "2", "1000").forEach { level ->
                assert(GameCatalog.isValidLevel(level))
            }
            listOf("", "0", "-1", "abc", "1.5").forEach { level ->
                assert(!GameCatalog.isValidLevel(level))
            }
        }
    }
})

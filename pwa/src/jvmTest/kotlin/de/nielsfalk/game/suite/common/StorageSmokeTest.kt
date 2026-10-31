package de.nielsfalk.game.suite.common

import io.kotest.core.spec.style.FreeSpec

class StorageSmokeTest : FreeSpec({
    "PWA storage smoke tests" - {
        "should store and retrieve username" - {
            data class UsernameCase(val username: String)

            val cases = listOf(
                UsernameCase("alice"),
                UsernameCase("bob"),
                UsernameCase("charlie123")
            )

            cases.forEach { (username) ->
                "username $username" {
                    val storage = ScoreStorageService(InMemoryStorageDriver())
                    assert(storage.getUsername() == null)

                    storage.setUsername(username)
                    assert(storage.getUsername() == username)
                }
            }
        }

        "should save and retrieve local scores" - {
            data class ScoreCase(
                val username: String,
                val gameName: String,
                val stage: String,
                val levelId: String,
                val time: Long,
                val points: Long
            )

            val cases = listOf(
                ScoreCase("alice", "memory", "easy", "1", 1500L, 120L),
                ScoreCase("bob", "memory", "hard", "2", 2300L, -30L),
                ScoreCase("charlie", "memory", "medium", "3", 4500L, 250L)
            )

            cases.forEach { (username, gameName, stage, levelId, time, points) ->
                "score for $username in $gameName/$stage/$levelId" {
                    val storage = ScoreStorageService(InMemoryStorageDriver())
                    assert(storage.getLocalScores().isEmpty())

                    val score = Score(username, gameName, stage, levelId, time, points)
                    storage.saveScoreLocally(score)

                    val scores = storage.getLocalScores()
                    assert(scores.size == 1)
                    assert(scores.first() == score)
                    assert(scores.first().points == points)
                }
            }
        }

        "should persist scores across multiple saves" {
            val storage = ScoreStorageService(InMemoryStorageDriver())
            val score1 = Score("alice", "memory", "easy", "1", 1000L, 100L)
            val score2 = Score("alice", "memory", "hard", "2", 2000L, 200L)

            storage.saveScoreLocally(score1)
            storage.saveScoreLocally(score2)

            val scores = storage.getLocalScores()
            assert(scores.size == 2)
            assert(scores.contains(score1))
            assert(scores.contains(score2))
        }

        "should clear the username without removing local scores" {
            val storage = ScoreStorageService(InMemoryStorageDriver())
            val score = Score("alice", "minesweeper", "easy", "42", 1000L, -30L)
            storage.setUsername("alice")
            storage.saveScoreLocally(score)

            storage.clearUsername()

            assert(storage.getUsername() == null)
            assert(storage.getLocalScores() == listOf(score))
        }

        "should return empty list when no scores stored" {
            val storage = ScoreStorageService(InMemoryStorageDriver())
            assert(storage.getLocalScores().isEmpty())
        }

        "should handle corrupted score data gracefully" {
            val driver = InMemoryStorageDriver()
            driver.setItem("gamesuite_scores", "not-valid-json")
            val storage = ScoreStorageService(driver)
            assert(storage.getLocalScores().isEmpty())
        }
    }
})

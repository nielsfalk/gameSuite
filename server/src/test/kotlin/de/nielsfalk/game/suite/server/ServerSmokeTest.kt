package de.nielsfalk.game.suite.server

import de.nielsfalk.game.suite.common.HighscoreSummary
import de.nielsfalk.game.suite.common.Score
import de.nielsfalk.game.suite.common.UserSummary
import io.kotest.core.spec.style.FreeSpec
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication

class ServerSmokeTest : FreeSpec({
    "Server module smoke tests" - {
        "should handle root and health endpoints" {
            testApplication {
                application { module() }
                val response = client.get("/")
                assert(response.status == HttpStatusCode.OK)

                val healthResponse = client.get("/health")
                assert(healthResponse.status == HttpStatusCode.OK)
            }
        }

        "should post and get scores without auth across various games and stages" - {
            data class ScoreTestCase(
                val username: String,
                val gameName: String,
                val stage: String,
                val level: String,
                val time: Long,
                val points: Long
            )

            val testCases = listOf(
                ScoreTestCase("alice", "minesweeper", "easy", "1", 1500L, 120L),
                ScoreTestCase("bob", "minesweeper", "beginner", "2", 2300L, -30L),
                ScoreTestCase("charlie", "minesweeper", "expert", "3", 4500L, 250L),
                ScoreTestCase("dana", "hexmine", "easy", "4", 1800L, 90L),
                ScoreTestCase("erin", "hexmine", "beginner", "5", 2100L, -40L),
                ScoreTestCase("frank", "hexlaser", "absolute_beginner", "6", 900L, 35L),
                ScoreTestCase("grace", "hexlaser", "hard", "7", 3200L, 120L),
                ScoreTestCase("heidi", "hexlaser", "nightmare_plus2", "8", 5400L, 240L)
            )

            testApplication {
                application { module() }
                val jsonClient = createClient {
                    install(ContentNegotiation) {
                        json()
                    }
                }

                testCases.forEach { testCase ->
                    // 1. Post score without auth
                    val postResponse = jsonClient.post("/${testCase.username}/${testCase.gameName}/${testCase.stage}/${testCase.level}") {
                        contentType(ContentType.Application.Json)
                        setBody(ScoreSubmission(time = testCase.time, points = testCase.points))
                    }
                    assert(postResponse.status == HttpStatusCode.Created)
                    val savedScore: Score = postResponse.body()
                    assert(savedScore.username == testCase.username)
                    assert(savedScore.points == testCase.points)
                    assert(savedScore.time == testCase.time)
                    assert(savedScore.levelId == testCase.level)

                    // 2. Get user scores for specific path
                    val getResponse = jsonClient.get("/${testCase.username}/${testCase.gameName}/${testCase.stage}/${testCase.level}")
                    assert(getResponse.status == HttpStatusCode.OK)
                    val scores: List<Score> = getResponse.body()
                    assert(scores.any { it.username == testCase.username && it.points == testCase.points })

                    // 3. Highscores summary check
                    val summaryResponse = jsonClient.get("/highscores/${testCase.gameName}/${testCase.stage}/${testCase.level}")
                    assert(summaryResponse.status == HttpStatusCode.OK)
                    val summary: HighscoreSummary = summaryResponse.body()
                    assert(summary.gameName == testCase.gameName)
                    assert(summary.topScores.isNotEmpty())
                }
            }
        }

        "should rank the level highscore by points then time and ignore losses" {
            testApplication {
                application { module() }
                val jsonClient = createClient {
                    install(ContentNegotiation) { json() }
                }

                val submissions = listOf(
                    Triple("slow", 100L, 30L to true),
                    Triple("fast", 100L, 10L to true),
                    Triple("best", 250L, 99L to true),
                    Triple("lost", 999L, 1L to false)
                )
                submissions.forEach { (player, points, attempt) ->
                    val (time, won) = attempt
                    val response = jsonClient.post("/$player/minesweeper/easy/7") {
                        contentType(ContentType.Application.Json)
                        setBody(ScoreSubmission(time = time, points = points, won = won))
                    }
                    assert(response.status == HttpStatusCode.Created)
                }

                val summary: HighscoreSummary = jsonClient.get("/highscores/minesweeper/easy/7").body()
                assert(summary.topScores.map { it.username } == listOf("best", "fast", "slow"))
                assert(summary.highestScore == 250L)
                assert(summary.totalPlays == 4L)
            }
        }

        "should summarize the scores of one user per game and stage" {
            testApplication {
                application { module() }
                val jsonClient = createClient {
                    install(ContentNegotiation) { json() }
                }

                val scores = listOf(
                    "/sumalice/minesweeper/easy/1" to 100L,
                    "/sumalice/minesweeper/easy/2" to 300L,
                    "/sumalice/hexmine/beginner/1" to 50L,
                    "/sumbob/minesweeper/easy/1" to 999L
                )
                scores.forEach { (path, points) ->
                    val response = jsonClient.post(path) {
                        contentType(ContentType.Application.Json)
                        setBody(ScoreSubmission(time = 10L, points = points))
                    }
                    assert(response.status == HttpStatusCode.Created)
                }

                val summary: UserSummary = jsonClient.get("/summary/sumalice").body()
                assert(summary.username == "sumalice")
                assert(summary.entries.size == 2)

                val minesweeper = summary.entries.first { it.gameName == "minesweeper" }
                assert(minesweeper.stage == "easy")
                assert(minesweeper.levelsPlayed == 2)
                assert(minesweeper.levelsWon == 2)
                assert(minesweeper.bestPoints == 300L)
                assert(minesweeper.totalPoints == 400L)
                assert(minesweeper.highestLevelPlayed == 2)
            }
        }

        "should return an empty summary for unknown users" {
            testApplication {
                application { module() }
                val jsonClient = createClient {
                    install(ContentNegotiation) { json() }
                }

                val summary: UserSummary = jsonClient.get("/summary/nobody").body()
                assert(summary.username == "nobody")
                assert(summary.entries.isEmpty())
            }
        }

        "should reject unknown games, stages and invalid levels before accepting scores" {
            testApplication {
                application { module() }
                val jsonClient = createClient {
                    install(ContentNegotiation) {
                        json()
                    }
                }

                val unknownGamePost = jsonClient.post("/alice/unknown/easy/1") {
                    contentType(ContentType.Application.Json)
                    setBody(ScoreSubmission(time = 1000L, points = 10L))
                }
                assert(unknownGamePost.status == HttpStatusCode.BadRequest)

                val unknownStagePost = jsonClient.post("/alice/minesweeper/hard/1") {
                    contentType(ContentType.Application.Json)
                    setBody(ScoreSubmission(time = 1000L, points = 10L))
                }
                assert(unknownStagePost.status == HttpStatusCode.BadRequest)

                val unknownGameGet = jsonClient.get("/alice/unknown/easy/1")
                assert(unknownGameGet.status == HttpStatusCode.BadRequest)

                val unknownStageGet = jsonClient.get("/alice/minesweeper/hard/1")
                assert(unknownStageGet.status == HttpStatusCode.BadRequest)

                val unknownGameHighscores = jsonClient.get("/highscores/unknown/easy/1")
                assert(unknownGameHighscores.status == HttpStatusCode.BadRequest)

                val unknownStageHighscores = jsonClient.get("/highscores/minesweeper/hard/1")
                assert(unknownStageHighscores.status == HttpStatusCode.BadRequest)

                val unknownHexlaserStagePost = jsonClient.post("/alice/hexlaser/nightmare_plus3/2") {
                    contentType(ContentType.Application.Json)
                    setBody(ScoreSubmission(time = 1000L, points = 10L))
                }
                assert(unknownHexlaserStagePost.status == HttpStatusCode.BadRequest)

                val droppedCustomStagePost = jsonClient.post("/alice/minesweeper/custom-9x9-10/1") {
                    contentType(ContentType.Application.Json)
                    setBody(ScoreSubmission(time = 1000L, points = 10L))
                }
                assert(droppedCustomStagePost.status == HttpStatusCode.BadRequest)

                val invalidLevelPost = jsonClient.post("/alice/minesweeper/easy/0") {
                    contentType(ContentType.Application.Json)
                    setBody(ScoreSubmission(time = 1000L, points = 10L))
                }
                assert(invalidLevelPost.status == HttpStatusCode.BadRequest)

                val nonNumericLevelPost = jsonClient.post("/alice/minesweeper/easy/abc") {
                    contentType(ContentType.Application.Json)
                    setBody(ScoreSubmission(time = 1000L, points = 10L))
                }
                assert(nonNumericLevelPost.status == HttpStatusCode.BadRequest)

                val validLevelPost = jsonClient.post("/alice/minesweeper/easy/1") {
                    contentType(ContentType.Application.Json)
                    setBody(ScoreSubmission(time = 1000L, points = 10L))
                }
                assert(validLevelPost.status == HttpStatusCode.Created)
            }
        }
    }
})

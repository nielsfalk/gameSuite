package de.nielsfalk.game.suite.server

import de.nielsfalk.game.suite.common.HighscoreSummary
import de.nielsfalk.game.suite.common.Score
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
                val levelId: String,
                val time: Long,
                val points: Long
            )

            val testCases = listOf(
                ScoreTestCase("alice", "minesweeper", "easy", "1", 1500L, 120L),
                ScoreTestCase("bob", "minesweeper", "beginner", "2", 2300L, -30L),
                ScoreTestCase("charlie", "minesweeper", "expert", "3", 4500L, 250L),
                ScoreTestCase("dana", "hexmine", "easy", "4", 1800L, 90L),
                ScoreTestCase("erin", "hexmine", "custom-7x7-5", "5", 2100L, -40L),
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
                    val postResponse = jsonClient.post("/${testCase.username}/${testCase.gameName}/${testCase.stage}/${testCase.levelId}") {
                        contentType(ContentType.Application.Json)
                        setBody(ScoreSubmission(time = testCase.time, points = testCase.points))
                    }
                    assert(postResponse.status == HttpStatusCode.Created)
                    val savedScore: Score = postResponse.body()
                    assert(savedScore.username == testCase.username)
                    assert(savedScore.points == testCase.points)
                    assert(savedScore.time == testCase.time)

                    // 2. Get user scores for specific path
                    val getResponse = jsonClient.get("/${testCase.username}/${testCase.gameName}/${testCase.stage}/${testCase.levelId}")
                    assert(getResponse.status == HttpStatusCode.OK)
                    val scores: List<Score> = getResponse.body()
                    assert(scores.any { it.username == testCase.username && it.points == testCase.points })

                    // 3. Highscores summary check
                    val summaryResponse = jsonClient.get("/highscores/${testCase.gameName}/${testCase.stage}/${testCase.levelId}")
                    assert(summaryResponse.status == HttpStatusCode.OK)
                    val summary: HighscoreSummary = summaryResponse.body()
                    assert(summary.gameName == testCase.gameName)
                    assert(summary.topScores.isNotEmpty())
                }
            }
        }

        "should reject unknown games and stages before accepting scores" {
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

                val validCustomStagePost = jsonClient.post("/alice/minesweeper/custom-9x9-10/1") {
                    contentType(ContentType.Application.Json)
                    setBody(ScoreSubmission(time = 1000L, points = 10L))
                }
                assert(validCustomStagePost.status == HttpStatusCode.Created)
            }
        }
    }
})

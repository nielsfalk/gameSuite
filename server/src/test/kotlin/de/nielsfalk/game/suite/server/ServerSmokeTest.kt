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
                ScoreTestCase("alice", "memory", "easy", "1", 1500L, 120L),
                ScoreTestCase("bob", "memory", "hard", "1", 2300L, -30L),
                ScoreTestCase("charlie", "memory", "medium", "2", 4500L, 250L)
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
    }
})

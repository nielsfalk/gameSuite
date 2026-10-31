package de.nielsfalk.game.suite.server

import de.nielsfalk.game.suite.common.GameCatalog
import de.nielsfalk.game.suite.common.Score
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class ScoreSubmission(
    val time: Long,
    val points: Long
)

private suspend fun ApplicationCall.rejectUnknownGameOrStage(gameName: String, stage: String): Boolean {
    if (!GameCatalog.isValidGame(gameName)) {
        respond(HttpStatusCode.BadRequest, mapOf("error" to "Unknown game: $gameName"))
        return false
    }
    if (!GameCatalog.isValidStage(gameName, stage)) {
        respond(
            HttpStatusCode.BadRequest,
            mapOf("error" to "Unknown stage '$stage' for game '$gameName'")
        )
        return false
    }
    return true
}

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module(repository: ScoreRepository = ScoreRepository()) {
    DatabaseFactory.init()

    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        anyHost()
    }

    install(CallLogging)

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (cause.message ?: "Internal Server Error")))
        }
    }

    routing {
        get("/") {
            call.respondText("Gamesuite Highscore/Multiplayer Server running")
        }

        get("/health") {
            call.respond(mapOf("status" to "ok"))
        }

        // Highscore summary endpoint
        get("/highscores/{gameName}/{stage}/{levelId}") {
            val gameName = call.parameters["gameName"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing gameName")
            val stage = call.parameters["stage"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing stage")
            val levelId = call.parameters["levelId"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing levelId")
            if (!call.rejectUnknownGameOrStage(gameName, stage)) return@get
            val summary = repository.getHighscores(gameName, stage, levelId)
            call.respond(summary)
        }

        // Score routes by path: /$username/$gameName/$stage/$levelId
        get("/{username}/{gameName}/{stage}/{levelId}") {
            val username = call.parameters["username"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing username")
            val gameName = call.parameters["gameName"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing gameName")
            val stage = call.parameters["stage"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing stage")
            val levelId = call.parameters["levelId"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing levelId")
            if (!call.rejectUnknownGameOrStage(gameName, stage)) return@get

            val scores = repository.getScores(username, gameName, stage, levelId)
            call.respond(scores)
        }

        // Post score without auth
        post("/{username}/{gameName}/{stage}/{levelId}") {
            val username = call.parameters["username"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing username")
            val gameName = call.parameters["gameName"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing gameName")
            val stage = call.parameters["stage"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing stage")
            val levelId = call.parameters["levelId"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing levelId")
            if (!call.rejectUnknownGameOrStage(gameName, stage)) return@post

            val submission = call.receive<ScoreSubmission>()
            val score = Score(
                username = username,
                gameName = gameName,
                stage = stage,
                levelId = levelId,
                time = submission.time,
                points = submission.points
            )
            val saved = repository.saveScore(score)
            call.respond(HttpStatusCode.Created, saved)
        }
    }
}

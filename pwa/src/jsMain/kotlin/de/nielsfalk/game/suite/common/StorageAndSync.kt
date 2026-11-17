package de.nielsfalk.game.suite.common

import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import org.w3c.fetch.RequestInit
import kotlin.js.json

class BrowserLocalStorageDriver : StorageDriver {
    override fun getItem(key: String): String? = try {
        localStorage.getItem(key)
    } catch (_: Throwable) {
        null
    }

    override fun setItem(key: String, value: String) {
        try {
            localStorage.setItem(key, value)
        } catch (_: Throwable) {
        }
    }

    override fun removeItem(key: String) {
        try {
            localStorage.removeItem(key)
        } catch (_: Throwable) {
        }
    }
}

/**
 * Talks to the score server and never fails when it is unreachable.
 *
 * Every submission is stored locally first. Reads return `null` when the server does not answer, so
 * callers can fall back to the locally cached scores. Being offline is never an error.
 */
class ScoreSyncService(
    val storageService: ScoreStorageService,
    private val serverBaseUrl: String = ServerConfig.resolve(
        try {
            localStorage.getItem(ServerConfig.OVERRIDE_STORAGE_KEY)
        } catch (_: Throwable) {
            null
        }
    ),
    private val json: Json = Json { ignoreUnknownKeys = true; isLenient = true }
) {
    /** Stores the score locally and tries to push it; failures are reported, never thrown. */
    suspend fun recordAndPushScore(
        score: Score,
        onStatus: (SyncStatus) -> Unit = {}
    ): SyncStatus {
        storageService.saveScoreLocally(score)
        val status = pushScore(score)
        onStatus(status)
        return status
    }

    /** Stores a score locally, so the offline fallback can show it immediately. */
    fun saveLocally(score: Score) {
        storageService.saveScoreLocally(score)
    }

    /** Pushes an already stored score; failures are reported, never thrown. */
    suspend fun pushScore(score: Score): SyncStatus =
        try {
            withTimeoutOrNull(ServerConfig.REQUEST_TIMEOUT_MILLIS) {
                val payload = js("({ time: 0, points: 0 })")
                payload.time = score.time as Number
                payload.points = score.points as Number
                val url = "$serverBaseUrl/${score.username}/${score.gameName}/${score.stage}/${score.levelId}"
                val response = window.fetch(
                    url,
                    RequestInit(
                        method = "POST",
                        headers = json("Content-Type" to "application/json"),
                        body = JSON.stringify(payload)
                    )
                ).await()
                if (response.ok) {
                    SyncStatus.Synced(score)
                } else {
                    SyncStatus.SavedLocallyOnly(score, "Server responded with status ${response.status}")
                }
            } ?: SyncStatus.SavedLocallyOnly(score, "Server did not answer in time")
        } catch (e: Throwable) {
            SyncStatus.SavedLocallyOnly(score, e.message ?: "Server unreachable")
        }

    /** The server highscore of one level, or `null` when the server is unreachable. */
    suspend fun fetchHighscore(gameName: String, stage: String, level: Int): HighscoreSummary? =
        fetch("/highscores/$gameName/$stage/${LevelSeed.format(level)}") {
            json.decodeFromString<HighscoreSummary>(it)
        }

    /** The server summary of one user, or `null` when the server is unreachable. */
    suspend fun fetchSummary(username: String): UserSummary? =
        fetch("/summary/$username") {
            json.decodeFromString<UserSummary>(it)
        }

    /** Locally cached scores, used as the offline fallback. */
    fun localScores(): List<Score> = storageService.getLocalScores()

    private suspend fun <T> fetch(path: String, decode: (String) -> T): T? = try {
        withTimeoutOrNull(ServerConfig.REQUEST_TIMEOUT_MILLIS) {
            val response = window.fetch("$serverBaseUrl$path").await()
            if (response.ok) {
                decode(response.text().await())
            } else {
                null
            }
        }
    } catch (_: Throwable) {
        null
    }
}

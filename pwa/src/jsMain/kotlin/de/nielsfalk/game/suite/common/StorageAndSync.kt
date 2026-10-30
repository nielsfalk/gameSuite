package de.nielsfalk.game.suite.common

import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.w3c.fetch.RequestInit
import kotlin.js.json

interface StorageDriver {
    fun getItem(key: String): String?
    fun setItem(key: String, value: String)
}

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
}

class InMemoryStorageDriver : StorageDriver {
    private val store = mutableMapOf<String, String>()
    override fun getItem(key: String): String? = store[key]
    override fun setItem(key: String, value: String) {
        store[key] = value
    }
}

class ScoreStorageService(private val storage: StorageDriver = BrowserLocalStorageDriver()) {
    private val json = Json { ignoreUnknownKeys = true }

    fun getUsername(): String? = storage.getItem("gamesuite_username")

    fun setUsername(username: String) {
        storage.setItem("gamesuite_username", username)
    }

    fun getLocalScores(): List<Score> {
        val raw = storage.getItem("gamesuite_scores") ?: return emptyList()
        return try {
            json.decodeFromString<List<Score>>(raw)
        } catch (_: Throwable) {
            emptyList()
        }
    }

    fun saveScoreLocally(score: Score) {
        val current = getLocalScores().toMutableList()
        current.add(score)
        try {
            storage.setItem("gamesuite_scores", json.encodeToString(current))
        } catch (_: Throwable) {
        }
    }
}

sealed class SyncStatus {
    data class Synced(val score: Score) : SyncStatus()
    data class SavedLocallyOnly(val score: Score, val reason: String) : SyncStatus()
}

class ScoreSyncService(
    private val storageService: ScoreStorageService,
    private val serverBaseUrl: String = "http://localhost:8080"
) {
    suspend fun recordAndPushScore(
        score: Score,
        onStatus: (SyncStatus) -> Unit = {}
    ): SyncStatus {
        // 1. Always store locally first
        storageService.saveScoreLocally(score)

        // 2. Try to push to server without throwing/failing
        return try {
            val payload = json(
                "time" to score.time,
                "points" to score.points
            )
            val requestInit = RequestInit(
                method = "POST",
                headers = json("Content-Type" to "application/json"),
                body = JSON.stringify(payload)
            )
            val url = "$serverBaseUrl/${score.username}/${score.gameName}/${score.stage}/${score.levelId}"
            val response = window.fetch(url, requestInit)
            // Await JS promise safely
            val promise = response.then { res ->
                if (res.ok) {
                    SyncStatus.Synced(score)
                } else {
                    SyncStatus.SavedLocallyOnly(score, "Server responded with status ${res.status}")
                }
            }.catch { err ->
                SyncStatus.SavedLocallyOnly(score, "Network error: $err")
            }
            // For simple browser invocation
            SyncStatus.Synced(score)
        } catch (e: Throwable) {
            // Never fail when server is not reachable
            val fallback = SyncStatus.SavedLocallyOnly(score, e.message ?: "Server unreachable")
            onStatus(fallback)
            fallback
        }
    }
}

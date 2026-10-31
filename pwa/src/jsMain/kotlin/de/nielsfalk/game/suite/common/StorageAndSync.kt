package de.nielsfalk.game.suite.common

import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
            response.then { res ->
                if (res.ok) {
                    onStatus(SyncStatus.Synced(score))
                } else {
                    onStatus(SyncStatus.SavedLocallyOnly(score, "Server responded with status ${res.status}"))
                }
            }.catch { err ->
                onStatus(SyncStatus.SavedLocallyOnly(score, "Network error: $err"))
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

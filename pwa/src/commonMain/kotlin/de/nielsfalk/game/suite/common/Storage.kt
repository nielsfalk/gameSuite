package de.nielsfalk.game.suite.common

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface StorageDriver {
    fun getItem(key: String): String?
    fun setItem(key: String, value: String)
    fun removeItem(key: String)
}

class InMemoryStorageDriver : StorageDriver {
    private val store = mutableMapOf<String, String>()
    override fun getItem(key: String): String? = store[key]
    override fun setItem(key: String, value: String) {
        store[key] = value
    }
    override fun removeItem(key: String) {
        store.remove(key)
    }
}

class ScoreStorageService(private val storage: StorageDriver = InMemoryStorageDriver()) {
    private val json = Json { ignoreUnknownKeys = true }

    fun getUsername(): String? = storage.getItem("gamesuite_username")

    fun setUsername(username: String) {
        storage.setItem("gamesuite_username", username)
    }

    fun clearUsername() {
        storage.removeItem("gamesuite_username")
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
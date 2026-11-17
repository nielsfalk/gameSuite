package de.nielsfalk.game.suite.common

/**
 * Configuration of the score server.
 *
 * The deployed Render service is the default. Local development points the PWA at another server by
 * storing a URL under [OVERRIDE_STORAGE_KEY] in `localStorage`.
 */
object ServerConfig {
    const val DEFAULT_BASE_URL: String = "https://gamesuite-server.onrender.com"
    const val OVERRIDE_STORAGE_KEY: String = "gamesuite_server_url"
    const val REQUEST_TIMEOUT_MILLIS: Long = 5000

    /** Resolves the server base URL from an optional stored override. */
    fun resolve(storedOverride: String?): String =
        storedOverride?.trim()?.takeIf { it.isNotEmpty() }?.trimEnd('/') ?: DEFAULT_BASE_URL
}

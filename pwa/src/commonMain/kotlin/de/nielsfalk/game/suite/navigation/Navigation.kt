package de.nielsfalk.game.suite.navigation

import de.nielsfalk.game.suite.common.LevelSeed

/**
 * Every URL the PWA understands.
 *
 * Deep links follow the same segment order as the server score routes: `/{username}/{gameName}/{stage}/{level}`.
 * The game route `/{username}/{gameName}` shows the stage selection of one game, which is only reachable
 * after a game logo was clicked on the user home.
 */
sealed class AppRoute {
    data object AskUsername : AppRoute()
    data class UserHome(val username: String) : AppRoute()
    data class GameHome(val username: String, val gameName: String) : AppRoute()
    data class GameDeepLink(
        val username: String,
        val gameName: String,
        val stage: String,
        val level: Int
    ) : AppRoute()

    fun toPath(basePath: String = "/"): String {
        val routePath = when (this) {
            is AskUsername -> "/"
            is UserHome -> "/$username"
            is GameHome -> "/$username/$gameName"
            is GameDeepLink -> "/$username/$gameName/$stage/${LevelSeed.format(level)}"
        }
        val normalizedBase = basePath.trimEnd('/')
        return if (normalizedBase.isEmpty()) routePath else normalizedBase + routePath
    }

    companion object {
        fun parse(
            path: String,
            storedUsername: String? = null,
            basePath: String = "/"
        ): AppRoute {
            val withoutBase = stripBasePath(path.trim().removePrefix("#"), basePath)
            val cleaned = withoutBase.trim().removePrefix("/").removeSuffix("/")
            if (cleaned.isEmpty()) {
                return storedUsernameOrAsk(storedUsername)
            }

            val segments = cleaned.split("/").filter { it.isNotEmpty() }
            return when (segments.size) {
                1 -> UserHome(username = segments[0])
                2 -> GameHome(username = segments[0], gameName = segments[1])
                4 -> GameDeepLink(
                    username = segments[0],
                    gameName = segments[1],
                    stage = segments[2],
                    level = LevelSeed.parse(segments[3])
                )
                else -> storedUsernameOrAsk(storedUsername)
            }
        }

        private fun storedUsernameOrAsk(storedUsername: String?): AppRoute =
            if (!storedUsername.isNullOrBlank()) {
                UserHome(storedUsername)
            } else {
                AskUsername
            }

        private fun stripBasePath(path: String, basePath: String): String {
            val normalizedBase = basePath.trimEnd('/')
            return when {
                normalizedBase.isEmpty() -> path
                path == normalizedBase -> ""
                path.startsWith("$normalizedBase/") -> path.removePrefix(normalizedBase)
                else -> path
            }
        }
    }
}

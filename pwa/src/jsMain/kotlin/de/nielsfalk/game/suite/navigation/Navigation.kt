package de.nielsfalk.game.suite.navigation

sealed class AppRoute {
    data object AskUsername : AppRoute()
    data class UserHome(val username: String) : AppRoute()
    data class GameDeepLink(
        val username: String,
        val stage: String,
        val gameName: String,
        val levelId: String
    ) : AppRoute()

    fun toPath(): String = when (this) {
        is AskUsername -> "/"
        is UserHome -> "/$username"
        is GameDeepLink -> "/$username/$stage/$gameName/$levelId"
    }

    companion object {
        fun parse(path: String, storedUsername: String? = null): AppRoute {
            val cleaned = path.trim().removePrefix("#").removePrefix("/").removeSuffix("/")
            if (cleaned.isEmpty()) {
                return if (!storedUsername.isNullOrBlank()) {
                    UserHome(storedUsername)
                } else {
                    AskUsername
                }
            }

            val segments = cleaned.split("/").filter { it.isNotEmpty() }
            return when (segments.size) {
                1 -> UserHome(username = segments[0])
                4 -> GameDeepLink(
                    username = segments[0],
                    stage = segments[1],
                    gameName = segments[2],
                    levelId = segments[3]
                )
                else -> {
                    if (!storedUsername.isNullOrBlank()) {
                        UserHome(storedUsername)
                    } else {
                        AskUsername
                    }
                }
            }
        }
    }
}

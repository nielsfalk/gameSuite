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

    fun toPath(basePath: String = "/"): String {
        val routePath = when (this) {
            is AskUsername -> "/"
            is UserHome -> "/$username"
            is GameDeepLink -> "/$username/$stage/$gameName/$levelId"
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

package de.nielsfalk.game.suite.navigation

import io.kotest.core.spec.style.FreeSpec

class NavigationSmokeTest : FreeSpec({
    "PWA navigation smoke tests" - {
        "should parse deep links into GameDeepLink routes" - {
            data class DeepLinkCase(
                val path: String,
                val expectedUsername: String,
                val expectedStage: String,
                val expectedGameName: String,
                val expectedLevelId: String
            )

            val cases = listOf(
                DeepLinkCase("/alice/easy/memory/1", "alice", "easy", "memory", "1"),
                DeepLinkCase("/bob/hard/memory/2", "bob", "hard", "memory", "2"),
                DeepLinkCase("/charlie/medium/game/3", "charlie", "medium", "game", "3")
            )

            cases.forEach { (path, username, stage, gameName, levelId) ->
                "deep link $path" {
                    val route = AppRoute.parse(path)
                    assert(route is AppRoute.GameDeepLink)
                    route as AppRoute.GameDeepLink
                    assert(route.username == username)
                    assert(route.stage == stage)
                    assert(route.gameName == gameName)
                    assert(route.levelId == levelId)
                }
            }
        }

        "should parse single-segment paths into UserHome routes" - {
            data class HomeCase(val path: String, val expectedUsername: String)

            val cases = listOf(
                HomeCase("/alice", "alice"),
                HomeCase("/bob", "bob")
            )

            cases.forEach { (path, username) ->
                "home path $path" {
                    val route = AppRoute.parse(path)
                    assert(route is AppRoute.UserHome)
                    route as AppRoute.UserHome
                    assert(route.username == username)
                }
            }
        }

        "should forward to stored username when path is empty" {
            val route = AppRoute.parse("/", storedUsername = "alice")
            assert(route is AppRoute.UserHome)
            assert((route as AppRoute.UserHome).username == "alice")
        }

        "should ask for username when path is empty and no stored username" {
            val route = AppRoute.parse("/", storedUsername = null)
            assert(route is AppRoute.AskUsername)
        }

        "should fall back to stored username for unrecognized paths" {
            val route = AppRoute.parse("/some/unknown/path", storedUsername = "alice")
            assert(route is AppRoute.UserHome)
            assert((route as AppRoute.UserHome).username == "alice")
        }

        "should round-trip routes through toPath and parse" - {
            data class RoundTripCase(val route: AppRoute, val expectedPath: String)

            val cases = listOf(
                RoundTripCase(AppRoute.UserHome("alice"), "/alice"),
                RoundTripCase(
                    AppRoute.GameDeepLink("bob", "hard", "memory", "2"),
                    "/bob/hard/memory/2"
                )
            )

            cases.forEach { (route, expectedPath) ->
                "round-trip $expectedPath" {
                    assert(route.toPath() == expectedPath)
                }
            }
        }
    }
})

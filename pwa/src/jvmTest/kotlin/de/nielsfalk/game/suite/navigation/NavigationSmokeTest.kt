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
                DeepLinkCase("/charlie/medium/game/3", "charlie", "medium", "game", "3"),
                DeepLinkCase(
                    "/dora/custom-12x10-20/minesweeper/123456789",
                    "dora",
                    "custom-12x10-20",
                    "minesweeper",
                    "123456789"
                )
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

        "should strip a deployment base path when parsing" - {
            val home = AppRoute.parse("/gameSuite/alice", basePath = "/gameSuite/")
            assert(home is AppRoute.UserHome)
            assert((home as AppRoute.UserHome).username == "alice")

            val deepLink = AppRoute.parse(
                path = "/gameSuite/alice/easy/hexmine/1",
                basePath = "/gameSuite/"
            )
            assert(deepLink is AppRoute.GameDeepLink)
            deepLink as AppRoute.GameDeepLink
            assert(deepLink.username == "alice")
            assert(deepLink.stage == "easy")
            assert(deepLink.gameName == "hexmine")
            assert(deepLink.levelId == "1")

            assert(AppRoute.parse("/gameSuite/", basePath = "/gameSuite/") is AppRoute.AskUsername)
            assert(AppRoute.parse("/gameSuite", basePath = "/gameSuite/") is AppRoute.AskUsername)

            val stored = AppRoute.parse(
                path = "/gameSuite/",
                storedUsername = "alice",
                basePath = "/gameSuite/"
            )
            assert(stored is AppRoute.UserHome)
            assert((stored as AppRoute.UserHome).username == "alice")
        }

        "should build paths under the deployment base path" {
            assert(AppRoute.AskUsername.toPath("/gameSuite/") == "/gameSuite/")
            assert(AppRoute.UserHome("alice").toPath("/gameSuite/") == "/gameSuite/alice")
            assert(
                AppRoute.GameDeepLink("alice", "easy", "hexmine", "1").toPath("/gameSuite/") ==
                    "/gameSuite/alice/easy/hexmine/1"
            )
            assert(AppRoute.UserHome("alice").toPath("/") == "/alice")
        }

        "should round-trip routes through toPath and parse" - {
            data class RoundTripCase(val route: AppRoute, val expectedPath: String)

            val cases = listOf(
                RoundTripCase(AppRoute.UserHome("alice"), "/alice"),
                RoundTripCase(
                    AppRoute.GameDeepLink("bob", "hard", "memory", "2"),
                    "/bob/hard/memory/2"
                ),
                RoundTripCase(
                    AppRoute.GameDeepLink("dora", "custom-12x10-20", "minesweeper", "123456789"),
                    "/dora/custom-12x10-20/minesweeper/123456789"
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

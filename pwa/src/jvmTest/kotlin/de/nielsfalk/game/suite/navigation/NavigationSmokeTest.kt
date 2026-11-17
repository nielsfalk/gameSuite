package de.nielsfalk.game.suite.navigation

import io.kotest.core.spec.style.FreeSpec

class NavigationSmokeTest : FreeSpec({
    "PWA navigation smoke tests" - {
        "should parse deep links into GameDeepLink routes" - {
            data class DeepLinkCase(
                val path: String,
                val expectedUsername: String,
                val expectedGameName: String,
                val expectedStage: String,
                val expectedLevel: Int
            )

            val cases = listOf(
                DeepLinkCase("/alice/minesweeper/easy/1", "alice", "minesweeper", "easy", 1),
                DeepLinkCase("/bob/hexmine/beginner/2", "bob", "hexmine", "beginner", 2),
                DeepLinkCase("/charlie/hexlaser/hard/3", "charlie", "hexlaser", "hard", 3),
                DeepLinkCase("/dora/minesweeper/expert/42", "dora", "minesweeper", "expert", 42)
            )

            cases.forEach { (path, username, gameName, stage, level) ->
                "deep link $path" {
                    val route = AppRoute.parse(path)
                    assert(route is AppRoute.GameDeepLink)
                    route as AppRoute.GameDeepLink
                    assert(route.username == username)
                    assert(route.gameName == gameName)
                    assert(route.stage == stage)
                    assert(route.level == level)
                }
            }
        }

        "should fall back to level 1 for legacy random level ids" - {
            listOf(
                "/alice/minesweeper/easy/123456789",
                "/alice/minesweeper/easy/abc",
                "/alice/minesweeper/easy/0"
            ).forEach { path ->
                "deep link $path" {
                    val route = AppRoute.parse(path)
                    assert(route is AppRoute.GameDeepLink)
                    assert((route as AppRoute.GameDeepLink).level == 1)
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

        "should parse two-segment paths into GameHome routes" - {
            data class GameCase(val path: String, val expectedUsername: String, val expectedGame: String)

            val cases = listOf(
                GameCase("/alice/minesweeper", "alice", "minesweeper"),
                GameCase("/alice/hexmine", "alice", "hexmine"),
                GameCase("/bob/hexlaser", "bob", "hexlaser")
            )

            cases.forEach { (path, username, gameName) ->
                "game path $path" {
                    val route = AppRoute.parse(path)
                    assert(route is AppRoute.GameHome)
                    route as AppRoute.GameHome
                    assert(route.username == username)
                    assert(route.gameName == gameName)
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

            val gameHome = AppRoute.parse("/gameSuite/alice/hexmine", basePath = "/gameSuite/")
            assert(gameHome is AppRoute.GameHome)
            gameHome as AppRoute.GameHome
            assert(gameHome.username == "alice")
            assert(gameHome.gameName == "hexmine")

            val deepLink = AppRoute.parse(
                path = "/gameSuite/alice/hexmine/easy/1",
                basePath = "/gameSuite/"
            )
            assert(deepLink is AppRoute.GameDeepLink)
            deepLink as AppRoute.GameDeepLink
            assert(deepLink.username == "alice")
            assert(deepLink.gameName == "hexmine")
            assert(deepLink.stage == "easy")
            assert(deepLink.level == 1)

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
            assert(AppRoute.GameHome("alice", "hexmine").toPath("/gameSuite/") == "/gameSuite/alice/hexmine")
            assert(
                AppRoute.GameDeepLink("alice", "hexmine", "easy", 1).toPath("/gameSuite/") ==
                    "/gameSuite/alice/hexmine/easy/1"
            )
            assert(AppRoute.UserHome("alice").toPath("/") == "/alice")
        }

        "should round-trip routes through toPath and parse" - {
            data class RoundTripCase(val route: AppRoute, val expectedPath: String)

            val cases = listOf(
                RoundTripCase(AppRoute.UserHome("alice"), "/alice"),
                RoundTripCase(AppRoute.GameHome("alice", "minesweeper"), "/alice/minesweeper"),
                RoundTripCase(
                    AppRoute.GameDeepLink("bob", "hexmine", "beginner", 2),
                    "/bob/hexmine/beginner/2"
                ),
                RoundTripCase(
                    AppRoute.GameDeepLink("dora", "minesweeper", "expert", 123456789),
                    "/dora/minesweeper/expert/123456789"
                )
            )

            cases.forEach { (route, expectedPath) ->
                "round-trip $expectedPath" {
                    assert(route.toPath() == expectedPath)
                    assert(AppRoute.parse(expectedPath) == route)
                }
            }
        }
    }
})

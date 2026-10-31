package de.nielsfalk.game.suite.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.nielsfalk.game.suite.common.ScoreStorageService
import de.nielsfalk.game.suite.common.ScoreSyncService
import kotlinx.browser.window
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.placeholder
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Li
import org.jetbrains.compose.web.dom.Option
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Select
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.Ul

@Composable
fun AppView(
    initialPath: String = window.location.pathname.ifBlank { window.location.hash },
    storageService: ScoreStorageService = ScoreStorageService(),
    syncService: ScoreSyncService = ScoreSyncService(storageService)
) {
    var currentRoute by remember {
        val storedUsername = storageService.getUsername()
        mutableStateOf(AppRoute.parse(initialPath, storedUsername))
    }

    var inputUsername by remember { mutableStateOf("") }
    var selectedStage by remember { mutableStateOf("easy") }
    var selectedLevel by remember { mutableStateOf("1") }

    fun navigateTo(route: AppRoute) {
        currentRoute = route
        val path = route.toPath()
        try {
            window.history.pushState(null, "", path)
        } catch (_: Throwable) {
        }
    }

    Div({ style { lollipopRoot() } }) {
        H1({ style { lollipopTitle() } }) { Text("Gamesuite") }

        when (val route = currentRoute) {
            is AppRoute.AskUsername -> {
                Div({ style { lollipopCard() } }) {
                    H2({ style { lollipopHeading() } }) { Text("Welcome to Gamesuite!") }
                    P({ style { lollipopParagraph() } }) { Text("Please choose a username to continue:") }

                    Input(type = InputType.Text) {
                        placeholder("Enter your username")
                        value(inputUsername)
                        onInput { inputUsername = it.value }
                        style { lollipopInput() }
                    }

                    Span({ style { property("display", "block"); property("height", "1rem") } })

                    Button({
                        style { lollipopButton() }
                        onClick {
                            if (inputUsername.isNotBlank()) {
                                storageService.setUsername(inputUsername.trim())
                                navigateTo(AppRoute.UserHome(inputUsername.trim()))
                            }
                        }
                    }) {
                        Text("Start Playing")
                    }
                }
            }

            is AppRoute.UserHome -> {
                Div({ style { lollipopCard() } }) {
                    H2({ style { lollipopHeading() } }) { Text("Hello, ${route.username}!") }
                    P({ style { lollipopParagraph() } }) { Text("Select game options to jump into a game level:") }

                    Div({ style { property("margin-bottom", "1rem") } }) {
                        Span({ style { lollipopPill() } }) { Text("Stage") }
                        Select({
                            style { lollipopInput() }
                            onChange { it.value?.let { stage -> selectedStage = stage } }
                        }) {
                            Option("easy") { Text("Easy") }
                            Option("medium") { Text("Medium") }
                            Option("hard") { Text("Hard") }
                        }

                        Span({ style { property("display", "inline-block"); property("width", "1rem") } })
                        Span({ style { lollipopPill() } }) { Text("Level") }
                        Input(type = InputType.Text) {
                            value(selectedLevel)
                            onInput { selectedLevel = it.value }
                            style { lollipopInput() }
                        }

                        Span({ style { property("display", "block"); property("height", "1rem") } })
                        Button({
                            style { lollipopButton() }
                            onClick {
                                navigateTo(
                                    AppRoute.GameDeepLink(
                                        username = route.username,
                                        stage = selectedStage,
                                        gameName = "game",
                                        levelId = selectedLevel
                                    )
                                )
                            }
                        }) {
                            Text("Launch Game")
                        }
                    }

                    H3({ style { lollipopSubheading() } }) { Text("Your Recent Local Scores") }
                    val scores = storageService.getLocalScores().filter { it.username == route.username }
                    if (scores.isEmpty()) {
                        P({ style { lollipopParagraph() } }) { Text("No scores recorded yet for ${route.username}.") }
                    } else {
                        Ul {
                            scores.forEach { score ->
                                Li({ style { lollipopScoreRow() } }) {
                                    Text("${score.gameName} · Stage: ${score.stage} · Level: ${score.levelId} · Points: ${score.points}")
                                }
                            }
                        }
                    }
                }
            }

            is AppRoute.GameDeepLink -> {
                Div({ style { lollipopCard() } }) {
                    H2({ style { lollipopHeading() } }) { Text("${route.gameName} Game") }
                    P({ style { lollipopParagraph() } }) {
                        Span({ style { lollipopPill() } }) { Text("Player: ${route.username}") }
                        Span({ style { lollipopPill() } }) { Text("Stage: ${route.stage}") }
                        Span({ style { lollipopPill() } }) { Text("Level: ${route.levelId}") }
                    }
                    P({ style { lollipopParagraph() } }) { Text("No game implemented yet for this level.") }

                    H3({ style { lollipopSubheading() } }) { Text("Your Highscore Summary") }
                    val scores = storageService.getLocalScores()
                        .filter { it.username == route.username && it.gameName == route.gameName }
                    if (scores.isEmpty()) {
                        P({ style { lollipopParagraph() } }) { Text("No scores recorded yet for ${route.username}.") }
                    } else {
                        Ul {
                            scores.sortedByDescending { it.points }.forEach { score ->
                                Li({ style { lollipopScoreRow() } }) {
                                    Text("Stage: ${score.stage} · Level: ${score.levelId} · Points: ${score.points}")
                                }
                            }
                        }
                    }

                    Button({
                        style { lollipopButtonSecondary() }
                        onClick { navigateTo(AppRoute.UserHome(route.username)) }
                    }) {
                        Text("Back to Home")
                    }
                }
            }
        }
    }
}

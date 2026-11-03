package de.nielsfalk.game.suite.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.nielsfalk.game.suite.common.ScoreStorageService
import de.nielsfalk.game.suite.common.ScoreSyncService
import de.nielsfalk.game.suite.hexmine.HexMineView
import de.nielsfalk.game.suite.minesweeper.MinesweeperStage
import de.nielsfalk.game.suite.minesweeper.MinesweeperView
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
import kotlin.random.Random

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
    var selectedGame by remember { mutableStateOf("minesweeper") }
    var selectedStage by remember { mutableStateOf(MinesweeperStage.Easy.name) }
    var customWidth by remember { mutableStateOf("12") }
    var customHeight by remember { mutableStateOf("12") }
    var customMines by remember { mutableStateOf("20") }

    fun navigateTo(route: AppRoute) {
        currentRoute = route
        val path = route.toPath()
        try {
            window.history.pushState(null, "", path)
        } catch (_: Throwable) {
        }
    }

    fun parseSelectedStage(): MinesweeperStage? = if (selectedStage == "custom") {
        val width = customWidth.toIntOrNull()
        val height = customHeight.toIntOrNull()
        val mines = customMines.toIntOrNull()
        if (width == null || height == null || mines == null) {
            null
        } else {
            runCatching {
                MinesweeperStage.Custom(width = width, height = height, mines = mines)
            }.getOrNull()
        }
    } else {
        MinesweeperStage.parse(selectedStage)
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
                val parsedStage = parseSelectedStage()

                Div({ style { lollipopCard() } }) {
                    H2({ style { lollipopHeading() } }) { Text("Hello, ${route.username}!") }
                    P({ style { lollipopParagraph() } }) { Text("Choose a game and stage to jump into:") }

                    Div({ style { property("margin-bottom", "1rem") } }) {
                        Span({ style { lollipopPill() } }) { Text("Game") }
                        Select({
                            style { lollipopInput() }
                            onChange { it.value?.let { game -> selectedGame = game } }
                        }) {
                            Option("minesweeper") { Text("Minesweeper") }
                            Option("hexmine") { Text("HexMine") }
                        }

                        Span({ style { property("display", "block"); property("height", "0.75rem") } })

                        Span({ style { lollipopPill() } }) { Text("Stage") }
                        Select({
                            style { lollipopInput() }
                            onChange { it.value?.let { stage -> selectedStage = stage } }
                        }) {
                            Option(MinesweeperStage.Easy.name) { Text("Easy") }
                            Option(MinesweeperStage.Beginner.name) { Text("Beginner") }
                            Option(MinesweeperStage.Expert.name) { Text("Expert") }
                            Option("custom") { Text("Custom") }
                        }

                        if (selectedStage == "custom") {
                            Div({ style { property("margin-top", "0.75rem") } }) {
                                Span({ style { lollipopPill() } }) { Text("Width") }
                                Input(type = InputType.Text) {
                                    value(customWidth)
                                    onInput { customWidth = it.value }
                                    style { lollipopInput() }
                                }

                                Span({ style { lollipopPill() } }) { Text("Height") }
                                Input(type = InputType.Text) {
                                    value(customHeight)
                                    onInput { customHeight = it.value }
                                    style { lollipopInput() }
                                }

                                Span({ style { lollipopPill() } }) { Text("Mines") }
                                Input(type = InputType.Text) {
                                    value(customMines)
                                    onInput { customMines = it.value }
                                    style { lollipopInput() }
                                }
                            }
                            P({ style { lollipopParagraph() } }) {
                                Text("Width and height must be 5–30; mines must leave room for a safe first click.")
                            }
                        }

                        Span({ style { property("display", "block"); property("height", "1rem") } })
                        Button({
                            style { lollipopButton() }
                            onClick {
                                val stage = parsedStage ?: return@onClick
                                navigateTo(
                                    AppRoute.GameDeepLink(
                                        username = route.username,
                                        stage = stage.name,
                                        gameName = selectedGame,
                                        levelId = Random.nextLong().toString()
                                    )
                                )
                            }
                        }) {
                            Text("Launch Game")
                        }

                        if (parsedStage == null) {
                            P({ style { lollipopParagraph() } }) {
                                Text("Please enter a valid custom stage before launching.")
                            }
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

                    Div({ style { property("margin-top", "1.25rem") } }) {
                        Button({
                            style { lollipopButtonSecondary() }
                            onClick {
                                storageService.clearUsername()
                                inputUsername = ""
                                navigateTo(AppRoute.AskUsername)
                            }
                        }) {
                            Text("Log out")
                        }
                    }
                }
            }

            is AppRoute.GameDeepLink -> {
                val stage = MinesweeperStage.parse(route.stage)
                val seed = route.levelId.toLongOrNull() ?: route.levelId.hashCode().toLong()

                when {
                    route.gameName != "minesweeper" && route.gameName != "hexmine" -> {
                        Div({ style { lollipopCard() } }) {
                            H2({ style { lollipopHeading() } }) { Text("${route.gameName} Game") }
                            P({ style { lollipopParagraph() } }) {
                                Span({ style { lollipopPill() } }) { Text("Player: ${route.username}") }
                                Span({ style { lollipopPill() } }) { Text("Stage: ${route.stage}") }
                                Span({ style { lollipopPill() } }) { Text("Level: ${route.levelId}") }
                            }
                            P({ style { lollipopParagraph() } }) { Text("No game implemented yet for this level.") }

                            Button({
                                style { lollipopButtonSecondary() }
                                onClick { navigateTo(AppRoute.UserHome(route.username)) }
                            }) {
                                Text("Back to Home")
                            }
                        }
                    }

                    stage == null -> {
                        Div({ style { lollipopCard() } }) {
                            H2({ style { lollipopHeading() } }) { Text("Unknown stage") }
                            P({ style { lollipopParagraph() } }) {
                                Text("'${route.stage}' is not a valid ${route.gameName} stage.")
                            }
                            Button({
                                style { lollipopButtonSecondary() }
                                onClick { navigateTo(AppRoute.UserHome(route.username)) }
                            }) {
                                Text("Back to Home")
                            }
                        }
                    }

                    route.gameName == "minesweeper" -> {
                        MinesweeperView(
                            username = route.username,
                            stage = stage,
                            seed = seed,
                            syncService = syncService,
                            onBack = { navigateTo(AppRoute.UserHome(route.username)) },
                            onNewGame = { newSeed ->
                                navigateTo(
                                    AppRoute.GameDeepLink(
                                        username = route.username,
                                        stage = stage.name,
                                        gameName = "minesweeper",
                                        levelId = newSeed.toString()
                                    )
                                )
                            }
                        )
                    }

                    else -> {
                        HexMineView(
                            username = route.username,
                            stage = stage,
                            seed = seed,
                            syncService = syncService,
                            onBack = { navigateTo(AppRoute.UserHome(route.username)) },
                            onNewGame = { newSeed ->
                                navigateTo(
                                    AppRoute.GameDeepLink(
                                        username = route.username,
                                        stage = stage.name,
                                        gameName = "hexmine",
                                        levelId = newSeed.toString()
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

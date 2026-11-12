package de.nielsfalk.game.suite.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.nielsfalk.game.suite.common.ScoreStorageService
import de.nielsfalk.game.suite.common.ScoreSyncService
import de.nielsfalk.game.suite.hexlaser.HexlaserView
import de.nielsfalk.game.suite.hexlaser.StageType
import de.nielsfalk.game.suite.hexmine.HexMineView
import de.nielsfalk.game.suite.minesweeper.MinesweeperStage
import de.nielsfalk.game.suite.minesweeper.MinesweeperView
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.placeholder
import org.jetbrains.compose.web.attributes.selected
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
import org.w3c.dom.HTMLScriptElement
import org.w3c.dom.events.Event
import kotlin.random.Random

@Composable
fun AppView(
    initialPath: String = window.location.pathname.ifBlank { window.location.hash },
    storageService: ScoreStorageService = ScoreStorageService(),
    syncService: ScoreSyncService = ScoreSyncService(storageService)
) {
    val basePath = remember { appBasePath() }
    var currentRoute by remember {
        val storedUsername = storageService.getUsername()
        mutableStateOf(AppRoute.parse(initialPath, storedUsername, basePath))
    }

    var inputUsername by remember { mutableStateOf("") }
    var selectedGame by remember { mutableStateOf("minesweeper") }
    var selectedStage by remember { mutableStateOf(MinesweeperStage.Easy.name) }
    var customWidth by remember { mutableStateOf("12") }
    var customHeight by remember { mutableStateOf("12") }
    var customMines by remember { mutableStateOf("20") }

    fun navigateTo(route: AppRoute) {
        currentRoute = route
        val path = route.toPath(basePath)
        try {
            window.history.pushState(null, "", path)
        } catch (_: Throwable) {
        }
    }

    DisposableEffect(Unit) {
        val listener: (Event) -> Unit = {
            currentRoute = AppRoute.parse(
                window.location.pathname.ifBlank { window.location.hash },
                storageService.getUsername(),
                basePath
            )
        }
        window.addEventListener("popstate", listener)
        window.addEventListener("hashchange", listener)
        onDispose {
            window.removeEventListener("popstate", listener)
            window.removeEventListener("hashchange", listener)
        }
    }

    fun parseMinesweeperStage(): MinesweeperStage? = if (selectedStage == "custom") {
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

    fun selectedStageName(): String? = when (selectedGame) {
        "hexlaser" -> StageType.parse(selectedStage)?.wireName
        else -> parseMinesweeperStage()?.name
    }

    fun defaultStageFor(gameName: String): String = when (gameName) {
        "hexlaser" -> StageType.ABSOLUTE_BEGINNER.wireName
        else -> MinesweeperStage.Easy.name
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
                val stageName = selectedStageName()

                Div({ style { lollipopCard() } }) {
                    H2({ style { lollipopHeading() } }) { Text("Hello, ${route.username}!") }
                    P({ style { lollipopParagraph() } }) { Text("Choose a game and stage to jump into:") }

                    Div({ style { property("margin-bottom", "1rem") } }) {
                        Span({ style { lollipopPill() } }) { Text("Game") }
                        Select({
                            style { lollipopInput() }
                            onChange {
                                it.value?.let { game ->
                                    selectedGame = game
                                    selectedStage = defaultStageFor(game)
                                }
                            }
                        }) {
                            Option("minesweeper", attrs = {
                                if (selectedGame == "minesweeper") selected()
                            }) { Text("Minesweeper") }
                            Option("hexmine", attrs = {
                                if (selectedGame == "hexmine") selected()
                            }) { Text("HexMine") }
                            Option("hexlaser", attrs = {
                                if (selectedGame == "hexlaser") selected()
                            }) { Text("Hexlaser") }
                        }

                        Span({ style { property("display", "block"); property("height", "0.75rem") } })

                        Span({ style { lollipopPill() } }) { Text("Stage") }
                        Select({
                            style { lollipopInput() }
                            onChange { it.value?.let { stage -> selectedStage = stage } }
                        }) {
                            if (selectedGame == "hexlaser") {
                                StageType.entries.forEach { stageType ->
                                    Option(stageType.wireName, attrs = {
                                        if (selectedStage == stageType.wireName) selected()
                                    }) {
                                        Text(stageType.name.replace('_', ' '))
                                    }
                                }
                            } else {
                                Option(MinesweeperStage.Easy.name, attrs = {
                                    if (selectedStage == MinesweeperStage.Easy.name) selected()
                                }) { Text("Easy") }
                                Option(MinesweeperStage.Beginner.name, attrs = {
                                    if (selectedStage == MinesweeperStage.Beginner.name) selected()
                                }) { Text("Beginner") }
                                Option(MinesweeperStage.Expert.name, attrs = {
                                    if (selectedStage == MinesweeperStage.Expert.name) selected()
                                }) { Text("Expert") }
                                Option("custom", attrs = {
                                    if (selectedStage == "custom") selected()
                                }) { Text("Custom") }
                            }
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
                                val stage = stageName ?: return@onClick
                                navigateTo(
                                    AppRoute.GameDeepLink(
                                        username = route.username,
                                        stage = stage,
                                        gameName = selectedGame,
                                        levelId = Random.nextLong().toString()
                                    )
                                )
                            }
                        }) {
                            Text("Launch Game")
                        }

                        if (stageName == null) {
                            P({ style { lollipopParagraph() } }) {
                                Text("Please enter a valid stage before launching.")
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
                val seed = route.levelId.toLongOrNull() ?: route.levelId.hashCode().toLong()
                val onBack = { navigateTo(AppRoute.UserHome(route.username)) }

                when (route.gameName) {
                    "minesweeper", "hexmine" -> {
                        val stage = MinesweeperStage.parse(route.stage)
                        when {
                            stage == null -> UnknownStageCard(
                                gameName = route.gameName,
                                stage = route.stage,
                                onBack = onBack
                            )

                            route.gameName == "minesweeper" -> MinesweeperView(
                                username = route.username,
                                stage = stage,
                                seed = seed,
                                syncService = syncService,
                                onBack = onBack,
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

                            else -> HexMineView(
                                username = route.username,
                                stage = stage,
                                seed = seed,
                                syncService = syncService,
                                onBack = onBack,
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

                    "hexlaser" -> {
                        val stageType = StageType.parse(route.stage)
                        if (stageType == null) {
                            UnknownStageCard(
                                gameName = route.gameName,
                                stage = route.stage,
                                onBack = onBack
                            )
                        } else {
                            HexlaserView(
                                username = route.username,
                                stageType = stageType,
                                seed = seed,
                                syncService = syncService,
                                onBack = onBack,
                                onNewGame = { newSeed ->
                                    navigateTo(
                                        AppRoute.GameDeepLink(
                                            username = route.username,
                                            stage = stageType.wireName,
                                            gameName = "hexlaser",
                                            levelId = newSeed.toString()
                                        )
                                    )
                                },
                                onStageChange = { newStage, newSeed ->
                                    navigateTo(
                                        AppRoute.GameDeepLink(
                                            username = route.username,
                                            stage = newStage.wireName,
                                            gameName = "hexlaser",
                                            levelId = newSeed.toString()
                                        )
                                    )
                                }
                            )
                        }
                    }

                    else -> UnknownGameCard(
                        gameName = route.gameName,
                        username = route.username,
                        stage = route.stage,
                        levelId = route.levelId,
                        onBack = onBack
                    )
                }
            }
        }
    }
}

@Composable
private fun UnknownGameCard(
    gameName: String,
    username: String,
    stage: String,
    levelId: String,
    onBack: () -> Unit
) {
    Div({ style { lollipopCard() } }) {
        H2({ style { lollipopHeading() } }) { Text("$gameName Game") }
        P({ style { lollipopParagraph() } }) {
            Span({ style { lollipopPill() } }) { Text("Player: $username") }
            Span({ style { lollipopPill() } }) { Text("Stage: $stage") }
            Span({ style { lollipopPill() } }) { Text("Level: $levelId") }
        }
        P({ style { lollipopParagraph() } }) { Text("No game implemented yet for this level.") }
        Button({
            style { lollipopButtonSecondary() }
            onClick { onBack() }
        }) {
            Text("Back to Home")
        }
    }
}

@Composable
private fun UnknownStageCard(gameName: String, stage: String, onBack: () -> Unit) {
    Div({ style { lollipopCard() } }) {
        H2({ style { lollipopHeading() } }) { Text("Unknown stage") }
        P({ style { lollipopParagraph() } }) {
            Text("'$stage' is not a valid $gameName stage.")
        }
        Button({
            style { lollipopButtonSecondary() }
            onClick { onBack() }
        }) {
            Text("Back to Home")
        }
    }
}

private fun appBasePath(): String {
    val script = document.querySelector("script[src$='gameSuite-pwa.js']") as? HTMLScriptElement
        ?: return "/"
    val origin = window.location.origin
    val src = script.src
    if (!src.startsWith(origin)) return "/"
    val directory = src.removePrefix(origin).substringBeforeLast('/')
    return if (directory.isEmpty()) "/" else "$directory/"
}

package de.nielsfalk.game.suite.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import de.nielsfalk.game.suite.common.GameCatalog
import de.nielsfalk.game.suite.common.LevelSeed
import de.nielsfalk.game.suite.common.ScoreStorageService
import de.nielsfalk.game.suite.common.ScoreSyncService
import de.nielsfalk.game.suite.common.SuiteGame
import de.nielsfalk.game.suite.common.UserSummaryView
import de.nielsfalk.game.suite.hexlaser.HexlaserView
import de.nielsfalk.game.suite.hexlaser.StageType
import de.nielsfalk.game.suite.hexmine.HexMineView
import de.nielsfalk.game.suite.minesweeper.MinesweeperStage
import de.nielsfalk.game.suite.minesweeper.MinesweeperView
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.placeholder
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Li
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.Ul
import org.w3c.dom.HTMLScriptElement
import org.w3c.dom.events.Event

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
    val scope = rememberCoroutineScope()

    var inputUsername by remember { mutableStateOf("") }

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

    fun goHome() {
        val route = currentRoute
        val username = when (route) {
            is AppRoute.AskUsername -> null
            is AppRoute.UserHome -> route.username
            is AppRoute.GameHome -> route.username
            is AppRoute.GameDeepLink -> route.username
        }
        if (username != null) {
            navigateTo(AppRoute.UserHome(username))
        } else {
            navigateTo(AppRoute.AskUsername)
        }
    }

    fun onLevel(username: String, gameName: String, stage: String, level: Int) {
        navigateTo(AppRoute.GameDeepLink(username, gameName, stage, level))
    }

    fun onStageUp(username: String, gameName: String, currentStage: String, nextStage: String) {
        navigateTo(AppRoute.GameDeepLink(username, gameName, nextStage, LevelSeed.FIRST_LEVEL))
    }

    Div({ style { lollipopRoot() } }) {
        H1({ style { lollipopTitle() } }) { Text("Gamesuite") }

        when (val route = currentRoute) {
            is AppRoute.AskUsername -> {
                AskUsernameView(
                    input = inputUsername,
                    onInput = { inputUsername = it },
                    onSubmit = {
                        if (inputUsername.isNotBlank()) {
                            storageService.setUsername(inputUsername.trim())
                            navigateTo(AppRoute.UserHome(inputUsername.trim()))
                        }
                    }
                )
            }

            is AppRoute.UserHome -> {
                UserHomeView(
                    username = route.username,
                    onSelectGame = { gameName ->
                        navigateTo(AppRoute.GameHome(route.username, gameName))
                    },
                    onLogout = {
                        storageService.clearUsername()
                        inputUsername = ""
                        navigateTo(AppRoute.AskUsername)
                    },
                    syncService = syncService,
                    storageService = storageService
                )
            }

            is AppRoute.GameHome -> {
                val game = GameCatalog.game(route.gameName)
                if (game == null) {
                    UnknownGameCard(
                        gameName = route.gameName,
                        onBack = { goHome() }
                    )
                } else {
                    GameHomeView(
                        username = route.username,
                        game = game,
                        currentStage = game.defaultStage,
                        onLaunch = { stage, level ->
                            navigateTo(AppRoute.GameDeepLink(route.username, game.name, stage, level))
                        },
                        onBack = { goHome() },
                        syncService = syncService,
                        storageService = storageService
                    )
                }
            }

            is AppRoute.GameDeepLink -> {
                val game = GameCatalog.game(route.gameName)
                if (game == null) {
                    UnknownGameCard(
                        gameName = route.gameName,
                        onBack = { goHome() }
                    )
                } else {
                    GameDeepLinkView(
                        route = route,
                        game = game,
                        syncService = syncService,
                        onHome = { goHome() },
                        onLevel = { level -> onLevel(route.username, route.gameName, route.stage, level) },
                        onStageUp = { nextStage ->
                            onStageUp(route.username, route.gameName, route.stage, nextStage)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AskUsernameView(
    input: String,
    onInput: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Div({ style { lollipopCard() } }) {
        H2({ style { lollipopHeading() } }) { Text("Welcome to Gamesuite!") }
        P({ style { lollipopParagraph() } }) { Text("Please choose a username to continue:") }

        Input(type = InputType.Text) {
            placeholder("Enter your username")
            value(input)
            onInput { onInput(it.value) }
            style { lollipopInput() }
        }

        Span({ style { property("display", "block"); property("height", "1rem") } })

        Button({
            style { lollipopButton() }
            onClick { onSubmit() }
        }) {
            Text("Start Playing")
        }
    }
}

@Composable
private fun UserHomeView(
    username: String,
    onSelectGame: (String) -> Unit,
    onLogout: () -> Unit,
    syncService: ScoreSyncService,
    storageService: ScoreStorageService
) {
    Div({ style { lollipopCard() } }) {
        H2({ style { lollipopHeading() } }) { Text("Hello, $username!") }
        P({ style { lollipopParagraph() } }) { Text("Pick a game and start playing.") }

        // Game logo grid
        Div({ style { lollipopLogoGrid() } }) {
            GameCatalog.games.forEach { game ->
                Div({
                    style { lollipopLogoCard() }
                    onClick { onSelectGame(game.name) }
                }) {
                    SvgMarkup(GameLogos.markup(game.name, sizePx = 64))
                    H3({ style { lollipopHeading() } }) { Text(game.displayName) }
                    P({ style { lollipopMutedText() } }) { Text(game.tagline) }
                }
            }
        }

        // Highscore summary under the logos
        UserSummaryPanel(
            username = username,
            syncService = syncService,
            storageService = storageService,
            onContinue = { gameName, stage, level ->
                onSelectGame(gameName)
            }
        )

        Div({ style { property("margin-top", "1.25rem") } }) {
            Button({
                style { lollipopButtonSecondary() }
                onClick { onLogout() }
            }) {
                Text("Log out")
            }
        }
    }
}

@Composable
private fun UserSummaryPanel(
    username: String,
    syncService: ScoreSyncService,
    storageService: ScoreStorageService,
    onContinue: (String, String, Int) -> Unit
) {
    val scope = rememberCoroutineScope()
    var summary by remember(username) { mutableStateOf<UserSummaryView?>(null) }
    var loaded by remember(username) { mutableStateOf(false) }

    if (!loaded) {
        loaded = true
        scope.launch {
            val remote = syncService.fetchSummary(username)
            summary = if (remote != null) {
                UserSummaryView.fromServer(remote)
            } else {
                UserSummaryView.fromLocal(syncService.localScores(), username)
            }
        }
    }

    val data = summary
    if (data == null) return

    H3({ style { lollipopSubheading() } }) { Text("Your Progress") }

    if (data.entries.isEmpty()) {
        P({ style { lollipopParagraph() } }) { Text("No levels played yet.") }
        return
    }

    data.entries.forEach { entry ->
        val game = GameCatalog.game(entry.gameName)
        val displayGame = game?.displayName ?: entry.gameName
        val stageLabel = if (game != null) labelOf(game, entry.stage) else entry.stage
        Div({ style { lollipopSummaryRow() } }) {
            Span({ style { property("font-weight", LollipopTheme.fontWeightTitle) } }) {
                Text("$displayGame · $stageLabel")
            }
            Div({ style { lollipopMutedText() } }) {
                Text("${entry.levelsPlayed} level(s) · ")
            }
            Span({ style { lollipopPill() } }) { Text("Best: ${entry.bestPoints ?: "—"}") }
            Span({ style { lollipopPill() } }) { Text("Total: ${entry.totalPoints}") }
        }
    }

    if (!data.online) {
        P({ style { lollipopMutedText() } }) { Text("Offline — showing local progress.") }
    }
}

@Composable
private fun GameHomeView(
    username: String,
    game: SuiteGame,
    currentStage: String,
    onLaunch: (String, Int) -> Unit,
    onBack: () -> Unit,
    syncService: ScoreSyncService,
    storageService: ScoreStorageService
) {
    var selectedStage by remember(currentStage) { mutableStateOf(currentStage) }

    Div({ style { lollipopCard() } }) {
        H2({ style { lollipopHeading() } }) { Text(game.displayName) }

        Div({ style { lollipopToolbar() } }) {
            IconButton(GameIcon.Home, onClick = onBack)
        }

        P({ style { lollipopParagraph() } }) { Text("Choose a stage and start on level 1.") }

        Div({ style { lollipopToolbar() } }) {
            game.stages.forEach { stage ->
                Button({
                    style {
                        lollipopStageButton(selected = selectedStage == stage)
                    }
                    onClick {
                        selectedStage = stage
                        onLaunch(stage, LevelSeed.FIRST_LEVEL)
                    }
                }) {
                    Text(stageLabelOf(game, stage))
                }
            }
        }
    }
}

@Composable
private fun GameDeepLinkView(
    route: AppRoute.GameDeepLink,
    game: SuiteGame,
    syncService: ScoreSyncService,
    onHome: () -> Unit,
    onLevel: (Int) -> Unit,
    onStageUp: (String) -> Unit
) {
    val level = route.level

    when (game.name) {
        "minesweeper", "hexmine" -> {
            val stage = MinesweeperStage.parse(route.stage)
            if (stage == null) {
                UnknownStageCard(game = game, stage = route.stage, onBack = onHome)
            } else if (game.name == "minesweeper") {
                MinesweeperView(
                    username = route.username,
                    gameInfo = game,
                    stage = stage,
                    level = level,
                    syncService = syncService,
                    onHome = onHome,
                    onLevel = onLevel,
                    onStageUp = onStageUp
                )
            } else {
                HexMineView(
                    username = route.username,
                    gameInfo = game,
                    stage = stage,
                    level = level,
                    syncService = syncService,
                    onHome = onHome,
                    onLevel = onLevel,
                    onStageUp = onStageUp
                )
            }
        }

        "hexlaser" -> {
            val stageType = StageType.parse(route.stage)
            if (stageType == null) {
                UnknownStageCard(game = game, stage = route.stage, onBack = onHome)
            } else {
                HexlaserView(
                    username = route.username,
                    gameInfo = game,
                    stageType = stageType,
                    level = level,
                    syncService = syncService,
                    onHome = onHome,
                    onLevel = onLevel,
                    onStageUp = onStageUp
                )
            }
        }

        else -> UnknownGameCard(gameName = game.name, onBack = onHome)
    }
}

private fun labelOf(game: SuiteGame, stage: String): String =
    if (game.name == "hexlaser") {
        stage.replace('_', ' ').replaceFirstChar { it.uppercase() }
    } else {
        stage.replaceFirstChar { it.uppercase() }
    }

@Composable
private fun UnknownGameCard(
    gameName: String,
    onBack: () -> Unit
) {
    Div({ style { lollipopCard() } }) {
        H2({ style { lollipopHeading() } }) { Text("$gameName Game") }
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
private fun UnknownStageCard(
    game: SuiteGame,
    stage: String,
    onBack: () -> Unit
) {
    Div({ style { lollipopCard() } }) {
        H2({ style { lollipopHeading() } }) { Text("Unknown stage") }
        P({ style { lollipopParagraph() } }) {
            Text("'$stage' is not a valid ${game.displayName} stage.")
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
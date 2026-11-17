package de.nielsfalk.game.suite.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.nielsfalk.game.suite.common.LevelHighscore
import de.nielsfalk.game.suite.common.LevelSeed
import de.nielsfalk.game.suite.common.Score
import de.nielsfalk.game.suite.common.ScoreSyncService
import de.nielsfalk.game.suite.common.SuiteGame
import de.nielsfalk.game.suite.common.WinStreak
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.StyleScope
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import kotlin.random.Random

/** How long the winning animation runs before the highscore dialog opens. */
const val WIN_ANIMATION_MILLIS: Long = 2000L

/** Renders a simple SVG icon from the inline icon markup. */
@Composable
fun SvgIcon(icon: GameIcon, sizePx: Int = 22) {
    val raw = Icons.markup(icon, sizePx)
    Span({
        classes("gamesuite-icon-slot")
        style { property("display", "inline-flex") }
    }) {
        // The JS init script swaps these for inner HTML
        Text(raw)
    }
}

/** Renders arbitrary inline SVG markup. */
@Composable
fun SvgMarkup(markup: String) {
    Div({
        classes("gamesuite-icon-slot")
        style { property("display", "inline-flex") }
    }) {
        Text(markup)
    }
}

/** A round icon button of the game toolbar. */
@Composable
fun IconButton(
    icon: GameIcon,
    onClick: () -> Unit,
    enabled: Boolean = true,
    active: Boolean = false
) {
    Button({
        ref {
            this.asDynamic().setAttribute("title", icon.label)
            this.asDynamic().setAttribute("aria-label", icon.label)
            this.asDynamic().setAttribute("aria-disabled", (!enabled).toString())
        }
        style { lollipopIconButton(enabled = enabled, active = active) }
        onClick { if (enabled) onClick() }
    }) {
        SvgIcon(icon)
    }
}

/**
 * The shared level toolbar: `🏠 ‖ << < ↻ > >> ‖ 🏆`.
 *
 * Levels start at 1, so going back is disabled at level 1 and never leaves the stage. Forward
 * navigation is always possible because every stage has unlimited levels.
 */
@Composable
fun GameToolbar(
    level: Int,
    onHome: () -> Unit,
    onLevel: (Int) -> Unit,
    onRestart: () -> Unit,
    onHighscore: () -> Unit
) {
    val canGoBack = level > LevelSeed.FIRST_LEVEL

    Div({ style { lollipopToolbar() } }) {
        IconButton(GameIcon.Home, onClick = onHome)
        Div({ style { lollipopToolbarSpacer() } }) {}
        IconButton(
            GameIcon.Back10,
            enabled = canGoBack,
            onClick = { onLevel(level - 10) }
        )
        IconButton(
            GameIcon.Back1,
            enabled = canGoBack,
            onClick = { onLevel(level - 1) }
        )
        IconButton(GameIcon.Refresh, onClick = onRestart)
        IconButton(GameIcon.Forward1, onClick = { onLevel(level + 1) })
        IconButton(GameIcon.Forward10, onClick = { onLevel(level + 10) })
        Div({ style { lollipopToolbarSpacer() } }) {}
        IconButton(GameIcon.Trophy, onClick = onHighscore)
    }
}

/** The card around every game: heading, level pill, status pills, toolbar and board. */
@Composable
fun GameShell(
    title: String,
    level: Int,
    statusPills: List<String>,
    message: String,
    onHome: () -> Unit,
    onLevel: (Int) -> Unit,
    onRestart: () -> Unit,
    onHighscore: () -> Unit,
    extraControls: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    Div({ style { lollipopCard() } }) {
        Div({
            style {
                property("display", "flex")
                property("align-items", "center")
                property("justify-content", "space-between")
                property("gap", "0.75rem")
                property("flex-wrap", "wrap")
            }
        }) {
            H3({ style { lollipopHeading() } }) { Text(title) }
            Span({ style { lollipopLevelPill() } }) { Text("Level $level") }
        }

        ValuedStatusBar(statusPills)

        P({ style { lollipopParagraph() } }) { Text(message) }

        GameToolbar(
            level = level,
            onHome = onHome,
            onLevel = onLevel,
            onRestart = onRestart,
            onHighscore = onHighscore
        )

        content()

        extraControls()
    }
}

@Composable
private fun ValuedStatusBar(pills: List<String>) {
    if (pills.isEmpty()) return
    Div({
        style {
            property("display", "flex")
            property("flex-wrap", "wrap")
            property("gap", "0.35rem")
            property("margin-bottom", "0.5rem")
        }
    }) {
        pills.forEach { pill ->
            Span({ style { lollipopPill() } }) { Text(pill) }
        }
    }
}

/**
 * One played level: result submission, win streak, winning animation and the two dialogs.
 *
 * The session outlives a restart of the level, so the streak only resets on a loss or an explicit
 * restart, and a stage change creates a new session.
 */
class GameSession(
    val username: String,
    val game: SuiteGame,
    val stage: String,
    val level: Int,
    private val syncService: ScoreSyncService
) {
    val streak: WinStreak = WinStreak()

    var highscoreOpen by mutableStateOf(false)
        private set

    var highscore by mutableStateOf<LevelHighscore?>(null)
        private set

    var highscoreLoading by mutableStateOf(false)
        private set

    var stageUpOpen by mutableStateOf(false)
        private set

    var nextStage by mutableStateOf<String?>(null)
        private set

    var celebrating by mutableStateOf(false)
        private set

    /** Called by the game when the level is solved. */
    fun recordWin(
        scope: CoroutineScope,
        time: Long,
        points: Long,
        celebrateMillis: Long = WIN_ANIMATION_MILLIS
    ) {
        val score = score(won = true, time = time, points = points)
        syncService.saveLocally(score)
        scope.launch { syncService.pushScore(score) }
        scope.launch {
            celebrating = true
            if (celebrateMillis > 0) delay(celebrateMillis)
            celebrating = false
            openHighscore(scope)
            if (streak.recordWin()) {
                val next = game.nextStage(stage)
                nextStage = next
                stageUpOpen = next != null
            }
        }
    }

    /** Called by the game when the level is lost. */
    fun recordLoss(scope: CoroutineScope, time: Long, points: Long) {
        streak.recordLoss()
        val score = score(won = false, time = time, points = points)
        syncService.saveLocally(score)
        scope.launch { syncService.pushScore(score) }
    }

    /** Called when the player restarts the current level. */
    fun restart() {
        streak.restart()
    }

    /** Opens the highscore dialog from the toolbar or after a win. */
    fun openHighscore(scope: CoroutineScope) {
        highscoreOpen = true
        highscoreLoading = true
        scope.launch {
            val remote = syncService.fetchHighscore(game.name, stage, level)
            highscore = remote?.let { LevelHighscore.fromServer(it, level) }
                ?: LevelHighscore.fromLocal(syncService.localScores(), game.name, stage, level)
            highscoreLoading = false
        }
    }

    fun closeHighscore() {
        highscoreOpen = false
    }

    fun dismissStageUp() {
        stageUpOpen = false
    }

    private fun score(won: Boolean, time: Long, points: Long): Score =
        Score(
            username = username,
            gameName = game.name,
            stage = stage,
            levelId = LevelSeed.format(level),
            time = time,
            points = points,
            won = won
        )
}

/** Confetti burst that plays while a level is celebrated. */
@Composable
fun WinConfetti(seed: Long) {
    Div({
        classes("lollipop-confetti")
        style {
            property("position", "absolute")
            property("inset", "0")
            property("overflow", "hidden")
            property("pointer-events", "none")
            property("border-radius", LollipopTheme.radius)
        }
    }) {
        confettiPieces(seed).forEach { piece ->
            Span({
                style {
                    property("position", "absolute")
                    property("top", "-24px")
                    property("left", "${piece.leftPercent}%")
                    property("width", "${piece.width}px")
                    property("height", "${piece.height}px")
                    property("background", piece.color)
                    property("border-radius", piece.radius)
                    property("animation", "lollipop-confetti-fall ${piece.durationMs}ms ease-in ${piece.delayMs}ms both")
                    property("--drift", "${piece.driftPx}px")
                    property("--spin", "${piece.spinDeg}deg")
                }
            })
        }
    }
}

private data class ConfettiPiece(
    val leftPercent: Int,
    val width: Int,
    val height: Int,
    val color: String,
    val radius: String,
    val delayMs: Int,
    val durationMs: Int,
    val driftPx: Int,
    val spinDeg: Int
)

private fun confettiPieces(seed: Long): List<ConfettiPiece> {
    val random = Random(seed)
    val colors = listOf(
        LollipopTheme.primary,
        LollipopTheme.secondary,
        LollipopTheme.accent,
        LollipopTheme.success,
        LollipopTheme.violet,
        LollipopTheme.orange
    )

    return List(42) {
        ConfettiPiece(
            leftPercent = random.nextInt(0, 101),
            width = random.nextInt(6, 13),
            height = random.nextInt(8, 17),
            color = colors[random.nextInt(colors.size)],
            radius = if (random.nextBoolean()) "50%" else "2px",
            delayMs = random.nextInt(0, 600),
            durationMs = random.nextInt(1200, 2200),
            driftPx = random.nextInt(-80, 81),
            spinDeg = random.nextInt(-720, 721)
        )
    }
}

/** The highscore dialog of the current level. */
@Composable
fun HighscoreDialog(
    username: String,
    gameTitle: String,
    stageLabel: String,
    level: Int,
    highscore: LevelHighscore?,
    loading: Boolean,
    onNextLevel: () -> Unit,
    onClose: () -> Unit
) {
    Div({ style { lollipopDialogBackdrop() } }) {
        Div({ style { lollipopDialogCard() } }) {
            H3({ style { lollipopHeading() } }) { Text("Highscore") }
            P({ style { lollipopParagraph() } }) {
                Text("$gameTitle · $stageLabel · Level $level")
            }

            when {
                loading -> P({ style { lollipopMutedText() } }) { Text("Loading the highscore…") }
                highscore == null || highscore.entries.isEmpty() -> P({ style { lollipopMutedText() } }) {
                    Text("No won attempt on this level yet. Be the first!")
                }
                else -> {
                    highscore.entries.forEachIndexed { index, entry ->
                        HighscoreRow(
                            rank = index + 1,
                            entry = entry,
                            isOwn = entry.username == username
                        )
                    }
                    Span({ style { lollipopMutedText() } }) {
                        Text("${highscore.totalPlays} attempt(s) on this level")
                    }
                }
            }

            if (highscore != null && !highscore.online) {
                P({ style { lollipopMutedText() } }) {
                    Text("Offline — showing the scores stored on this device.")
                }
            }

            Div({ style { lollipopToolbar() } }) {
                Button({
                    style { lollipopButton() }
                    onClick { onNextLevel() }
                }) {
                    Text("Next level")
                }
                Button({
                    style { lollipopButtonSecondary() }
                    onClick { onClose() }
                }) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
private fun HighscoreRow(rank: Int, entry: Score, isOwn: Boolean) {
    Div({ style { lollipopHighscoreRow(isOwn) } }) {
        Span({
            style {
                property("font-weight", LollipopTheme.fontWeightTitle)
                property("min-width", "1.6rem")
                property("color", LollipopTheme.accent)
            }
        }) { Text("$rank.") }
        Span({ style { property("flex", "1 1 auto") } }) { Text(entry.username) }
        Span({ style { lollipopPill() } }) { Text("${entry.points} pts") }
        Span({ style { lollipopPill() } }) { Text("${entry.time}s") }
    }
}

/** The stage-up dialog after three consecutive wins. */
@Composable
fun StageUpDialog(
    stageLabel: String,
    nextStageLabel: String,
    onStageUp: () -> Unit,
    onStay: () -> Unit
) {
    Div({ style { lollipopDialogBackdrop() } }) {
        Div({ style { lollipopDialogCard() } }) {
            H3({ style { lollipopHeading() } }) { Text("Stage up!") }
            P({ style { lollipopParagraph() } }) {
                Text("Three wins in a row on $stageLabel. Ready for $nextStageLabel?")
            }
            Div({ style { lollipopToolbar() } }) {
                Button({
                    style { lollipopButton() }
                    onClick { onStageUp() }
                }) {
                    Text("Stage up")
                }
                Button({
                    style { lollipopButtonSecondary() }
                    onClick { onStay() }
                }) {
                    Text("Stay here")
                }
            }
        }
    }
}

/**
 * Renders the dialogs of [session]. The stage-up dialog waits until the highscore dialog is closed,
 * so a win never stacks two dialogs.
 */
@Composable
fun GameSessionDialogs(
    session: GameSession,
    gameTitle: String,
    stageLabel: String,
    onNextLevel: () -> Unit,
    onStageUp: (String) -> Unit
) {
    if (session.highscoreOpen) {
        HighscoreDialog(
            username = session.username,
            gameTitle = gameTitle,
            stageLabel = stageLabel,
            level = session.level,
            highscore = session.highscore,
            loading = session.highscoreLoading,
            onNextLevel = {
                session.closeHighscore()
                onNextLevel()
            },
            onClose = { session.closeHighscore() }
        )
    } else if (session.stageUpOpen) {
        val next = session.nextStage
        if (next != null) {
            StageUpDialog(
                stageLabel = stageLabel,
                nextStageLabel = stageLabelOf(session.game, next),
                onStageUp = {
                    session.dismissStageUp()
                    onStageUp(next)
                },
                onStay = { session.dismissStageUp() }
            )
        }
    }
}

/** Human readable label of a stage. */
fun stageLabelOf(game: SuiteGame, stage: String): String =
    if (game.name == "hexlaser") {
        stage.replace('_', ' ').replaceFirstChar { it.uppercase() }
    } else {
        stage.replaceFirstChar { it.uppercase() }
    }

/** Creates a session that survives recompositions of the same level. */
@Composable
fun rememberGameSession(
    username: String,
    game: SuiteGame,
    stage: String,
    level: Int,
    syncService: ScoreSyncService
): GameSession = remember(username, game.name, stage, level) {
    GameSession(username, game, stage, level, syncService)
}

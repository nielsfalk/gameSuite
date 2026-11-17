package de.nielsfalk.game.suite.minesweeper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import de.nielsfalk.game.suite.common.LevelSeed
import de.nielsfalk.game.suite.common.ScoreSyncService
import de.nielsfalk.game.suite.common.SuiteGame
import de.nielsfalk.game.suite.navigation.GameIcon
import de.nielsfalk.game.suite.navigation.GameSessionDialogs
import de.nielsfalk.game.suite.navigation.GameShell
import de.nielsfalk.game.suite.navigation.IconButton
import de.nielsfalk.game.suite.navigation.WinConfetti
import de.nielsfalk.game.suite.navigation.rememberGameSession
import de.nielsfalk.game.suite.navigation.stageLabelOf
import kotlinx.coroutines.delay
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import kotlin.js.Date

/**
 * Minesweeper of one numbered level.
 *
 * The level number seeds the board, so the same level always reproduces the same field. Restarting
 * with the refresh button replays that field with a fresh timer and fresh points.
 */
@Composable
fun MinesweeperView(
    username: String,
    gameInfo: SuiteGame,
    stage: MinesweeperStage,
    level: Int,
    syncService: ScoreSyncService,
    onHome: () -> Unit,
    onLevel: (Int) -> Unit,
    onStageUp: (String) -> Unit
) {
    val seed = remember(gameInfo.name, stage.name, level) {
        LevelSeed.of(gameInfo.name, stage.name, level)
    }
    var attempt by remember(gameInfo.name, stage.name, level) { mutableStateOf(0) }
    val game = remember(stage, level, attempt) { MinesweeperGame(stage, seed) }
    val session = rememberGameSession(username, gameInfo, stage.name, level, syncService)
    val scope = rememberCoroutineScope()

    var version by remember(stage, level, attempt) { mutableStateOf(0) }
    var flagMode by remember(stage, level, attempt) { mutableStateOf(false) }
    var startedAt by remember(stage, level, attempt) { mutableStateOf<Double?>(null) }
    var elapsedSeconds by remember(stage, level, attempt) { mutableStateOf(0L) }
    var submitted by remember(stage, level, attempt) { mutableStateOf(false) }

    fun updateElapsed() {
        val start = startedAt ?: return
        elapsedSeconds = ((Date.now() - start) / 1000.0).toLong()
    }

    fun handleReveal(x: Int, y: Int) {
        if (game.isOver) return
        val cell = game.cellAt(x, y)
        if (cell.revealed || cell.flagged) return
        if (!game.minesPlaced) startedAt = Date.now()
        game.reveal(x, y)
        if (game.isOver) updateElapsed()
        version++
    }

    fun handleFlag(x: Int, y: Int) {
        if (game.isOver) return
        game.toggleFlag(x, y)
        version++
    }

    fun restart() {
        session.restart()
        attempt++
        version = 0
        flagMode = false
        startedAt = null
        elapsedSeconds = 0
        submitted = false
    }

    LaunchedEffect(stage, level, attempt) {
        while (true) {
            delay(250)
            if (startedAt != null && !game.isOver) {
                updateElapsed()
            }
        }
    }

    LaunchedEffect(version) {
        if (submitted) return@LaunchedEffect
        when (game.status) {
            MinesweeperStatus.Won -> {
                submitted = true
                updateElapsed()
                session.recordWin(scope, time = elapsedSeconds, points = game.points)
            }

            MinesweeperStatus.Lost -> {
                submitted = true
                updateElapsed()
                session.recordLoss()
            }

            MinesweeperStatus.InProgress -> Unit
        }
    }

    GameShell(
        title = gameInfo.displayName,
        level = level,
        statusPills = listOf(
            "Mines: ${game.minesRemaining}",
            "Time: ${elapsedSeconds}s",
            "Points: ${game.points}"
        ),
        message = when (game.status) {
            MinesweeperStatus.Won -> "You cleared the board!"
            MinesweeperStatus.Lost -> "Boom! You hit a mine."
            MinesweeperStatus.InProgress -> "Tap to reveal. Right-click or use flag mode to flag."
        },
        onHome = onHome,
        onLevel = onLevel,
        onRestart = { restart() },
        onHighscore = { session.openHighscore(scope) },
        extraControls = {
            IconButton(
                icon = GameIcon.Flag,
                onClick = { flagMode = !flagMode },
                active = flagMode
            )
        }
    ) {
        if (session.celebrating) {
            WinConfetti(seed = level.toLong())
        }

        Div({ style { minesweeperBoard(stage.width) } }) {
            for (y in 0 until stage.height) {
                for (x in 0 until stage.width) {
                    val cell = game.cellAt(x, y)
                    Div({
                        style { minesweeperCell(cell, game.status) }
                        onClick {
                            if (flagMode) handleFlag(x, y) else handleReveal(x, y)
                        }
                        onContextMenu {
                            it.preventDefault()
                            handleFlag(x, y)
                        }
                    }) {
                        Text(minesweeperCellLabel(cell))
                    }
                }
            }
        }

        P({ style { minesweeperHint() } }) {
            Text(
                "Level ${level} · ${stage.width}x${stage.height} with ${stage.mines} mines · seed $seed"
            )
        }

        Span({ style { minesweeperHint() } }) {
            Text(if (flagMode) "Flag mode is on." else "Flag mode is off.")
        }
    }

    GameSessionDialogs(
        session = session,
        gameTitle = gameInfo.displayName,
        stageLabel = stageLabelOf(gameInfo, stage.name),
        onNextLevel = { onLevel(level + 1) },
        onStageUp = onStageUp
    )
}

private fun minesweeperCellLabel(cell: MinesweeperCell): String = when {
    cell.revealed && cell.hasMine -> "*"
    cell.revealed && cell.adjacentMines > 0 -> cell.adjacentMines.toString()
    cell.revealed -> ""
    cell.flagged -> "F"
    else -> ""
}

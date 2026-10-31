package de.nielsfalk.game.suite.minesweeper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.nielsfalk.game.suite.common.Score
import de.nielsfalk.game.suite.common.ScoreSyncService
import de.nielsfalk.game.suite.navigation.lollipopButton
import de.nielsfalk.game.suite.navigation.lollipopButtonSecondary
import de.nielsfalk.game.suite.navigation.lollipopCard
import de.nielsfalk.game.suite.navigation.lollipopHeading
import de.nielsfalk.game.suite.navigation.lollipopParagraph
import de.nielsfalk.game.suite.navigation.lollipopPill
import kotlinx.coroutines.delay
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import kotlin.js.Date
import kotlin.random.Random

@Composable
fun MinesweeperView(
    username: String,
    stage: MinesweeperStage,
    seed: Long,
    syncService: ScoreSyncService,
    onBack: () -> Unit,
    onNewGame: (Long) -> Unit
) {
    val game = remember(stage, seed) { MinesweeperGame(stage, seed) }
    var version by remember(stage, seed) { mutableStateOf(0) }
    var flagMode by remember(stage, seed) { mutableStateOf(false) }
    var startedAt by remember(stage, seed) { mutableStateOf<Double?>(null) }
    var elapsedSeconds by remember(stage, seed) { mutableStateOf(0L) }
    var scoreSubmitted by remember(stage, seed) { mutableStateOf(false) }

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

    LaunchedEffect(stage, seed) {
        while (true) {
            delay(250)
            if (startedAt != null && !game.isOver) {
                updateElapsed()
            }
        }
    }

    LaunchedEffect(game.status) {
        if (game.status == MinesweeperStatus.Won && !scoreSubmitted) {
            scoreSubmitted = true
            syncService.recordAndPushScore(
                Score(
                    username = username,
                    gameName = "minesweeper",
                    stage = stage.name,
                    levelId = seed.toString(),
                    time = elapsedSeconds,
                    points = game.points
                )
            )
        }
    }

    Div({ style { lollipopCard() } }) {
        H2({ style { lollipopHeading() } }) {
            Text("Minesweeper · ${stage.name}")
        }

        Div({ style { minesweeperStatusBar(version) } }) {
            Span({ style { lollipopPill() } }) { Text("Mines: ${game.minesRemaining}") }
            Span({ style { lollipopPill() } }) { Text("Time: ${elapsedSeconds}s") }
            Span({ style { lollipopPill() } }) { Text("Points: ${game.points}") }
        }

        key(version) {
            P({ style { lollipopParagraph() } }) {
                Text(
                    when (game.status) {
                        MinesweeperStatus.Won -> "You cleared the board!"
                        MinesweeperStatus.Lost -> "Boom! You hit a mine."
                        MinesweeperStatus.InProgress -> "Tap to reveal. Right-click or use flag mode to flag."
                    }
                )
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
                Text("Seed: $seed · Stage: ${stage.width}x${stage.height} with ${stage.mines} mines")
            }

            Div({ style { minesweeperControls() } }) {
                Button({
                    style { if (flagMode) lollipopButton() else lollipopButtonSecondary() }
                    onClick { flagMode = !flagMode }
                }) {
                    Text(if (flagMode) "Flag mode: ON" else "Flag mode: OFF")
                }
                Button({
                    style { lollipopButtonSecondary() }
                    onClick { onNewGame(Random.nextLong()) }
                }) {
                    Text("New Game")
                }
                Button({
                    style { lollipopButtonSecondary() }
                    onClick { onBack() }
                }) {
                    Text("Back to Home")
                }
            }
        }
    }
}

private fun minesweeperCellLabel(cell: MinesweeperCell): String = when {
    cell.revealed && cell.hasMine -> "*"
    cell.revealed && cell.adjacentMines > 0 -> cell.adjacentMines.toString()
    cell.revealed -> ""
    cell.flagged -> "F"
    else -> ""
}

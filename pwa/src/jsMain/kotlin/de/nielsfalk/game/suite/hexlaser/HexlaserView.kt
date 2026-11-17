package de.nielsfalk.game.suite.hexlaser

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
import de.nielsfalk.game.suite.navigation.LollipopTheme
import de.nielsfalk.game.suite.navigation.stageLabelOf
import de.nielsfalk.game.suite.navigation.lollipopPill
import de.nielsfalk.game.suite.navigation.lollipopButton
import de.nielsfalk.game.suite.navigation.lollipopButtonSecondary
import de.nielsfalk.game.suite.navigation.lollipopCard
import de.nielsfalk.game.suite.navigation.lollipopHeading
import de.nielsfalk.game.suite.navigation.lollipopParagraph
import org.jetbrains.compose.web.css.StyleScope
import kotlinx.browser.window
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.dom.Canvas
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.events.Event
import kotlin.js.Date
import kotlin.math.abs
import kotlin.random.Random

/** Hexlaser of one numbered level, seeded by the level number. */
@Composable
fun HexlaserView(
    username: String,
    gameInfo: SuiteGame,
    stageType: StageType,
    level: Int,
    syncService: ScoreSyncService,
    onHome: () -> Unit,
    onLevel: (Int) -> Unit,
    onStageUp: (String) -> Unit
) {
    val seed = remember(gameInfo.name, stageType.wireName, level) {
        LevelSeed.of(gameInfo.name, stageType.wireName, level)
    }
    var attempt by remember(gameInfo.name, stageType.wireName, level) { mutableStateOf(0) }
    val game = remember(stageType, seed, attempt) { HexlasarGame(stageType, seed) }
    val session = rememberGameSession(username, gameInfo, stageType.wireName, level, syncService)
    val scope = rememberCoroutineScope()

    var version by remember(stageType, level, attempt) { mutableStateOf(0) }
    var canvasElement by remember(stageType, level, attempt) { mutableStateOf<HTMLCanvasElement?>(null) }
    var ongoingDrag by remember(stageType, level, attempt) { mutableStateOf<Pair<Position, Double>?>(null) }
    var startedAt by remember(stageType, level, attempt) { mutableStateOf<Double?>(null) }
    var elapsedSeconds by remember(stageType, level, attempt) { mutableStateOf(0L) }
    var submitted by remember(stageType, level, attempt) { mutableStateOf(false) }
    val rotationJob = remember(stageType, level, attempt) { mutableStateOf<Job?>(null) }

    fun beginIfNeeded() {
        if (startedAt == null) startedAt = Date.now()
    }

    fun currentElapsed(): Long =
        startedAt?.let { ((Date.now() - it) / 1000.0).toLong() } ?: 0L

    suspend fun animateWinning() {
        var spent = 0
        while (spent <= 3000) {
            game.setAnimationSpendTime(spent)
            version++
            delay(16)
            spent += 16
        }
        game.setAnimationSpendTime(null)
        version++
    }

    suspend fun animateRotation(position: Position, rotations: Int) {
        val cell = game.grid[position]
        if (cell.locked || rotations == 0) return
        beginIfNeeded()
        val steps = 8
        repeat(steps) { step ->
            game.setRotatedParts(
                position,
                rotations * 60 * (step + 1) / steps
            )
            version++
            delay(18)
        }
        game.setRotatedParts(position, 0)
        game.rotate(position, rotations)
        version++
        if (!submitted && game.solved) {
            submitted = true
            elapsedSeconds = currentElapsed()
            scope.launch {
                animateWinning()
                session.recordWin(scope, time = elapsedSeconds, points = game.points)
            }
        }
    }

    fun startRotation(position: Position, rotations: Int) {
        if (rotationJob.value?.isActive == true || game.solvedAndLocked) return
        rotationJob.value = scope.launch {
            animateRotation(position, rotations)
        }
    }

    fun handleHint() {
        if (rotationJob.value?.isActive == true || game.solvedAndLocked) return
        beginIfNeeded()
        game.requestHint()
        version++
        if (!submitted && game.solved) {
            submitted = true
            elapsedSeconds = currentElapsed()
            scope.launch {
                animateWinning()
                session.recordWin(scope, time = elapsedSeconds, points = game.points)
            }
        }
    }

    fun restart() {
        session.restart()
        attempt++
        version = 0
        startedAt = null
        elapsedSeconds = 0
        submitted = false
        game.retry()
    }

    LaunchedEffect(game, stageType, level, attempt) {
        while (true) {
            delay(14L)
            if (game.followGlow()) version++
        }
    }

    LaunchedEffect(game, stageType, level, attempt) {
        while (true) {
            delay(250)
            if (startedAt != null) {
                elapsedSeconds = currentElapsed()
            }
        }
    }

    LaunchedEffect(game, stageType, level, attempt, version, ongoingDrag, canvasElement) {
        val canvas = canvasElement ?: return@LaunchedEffect
        val width = canvas.clientWidth.toDouble()
        val height = canvas.clientHeight.toDouble()
        if (width <= 0 || height <= 0) return@LaunchedEffect
        if (canvas.width != width.toInt() || canvas.height != height.toInt()) {
            canvas.width = width.toInt()
            canvas.height = height.toInt()
        }
        val context = canvas.getContext("2d") as? CanvasRenderingContext2D ?: return@LaunchedEffect
        context.drawHexlaser(game, width, height, ongoingDrag)

        if (!game.grid.started && game.grid.x != game.grid.y) {
            val wantWide = width > height
            val isWide = game.grid.x > game.grid.y
            if (wantWide != isWide) {
                game.setToggleXY(!game.toggleXY)
                version++
            }
        }
    }

    GameShell(
        title = gameInfo.displayName,
        level = level,
        statusPills = listOf(
            "Moves: ${game.performedRotations}",
            "Time: ${elapsedSeconds}s",
            "Points: ${game.points}"
        ),
        message = when {
            game.solvedAndLocked -> "Solved! Tap the board or press forward to continue."
            session.celebrating -> "Level solved!"
            else -> "Tap to rotate, drag vertically to spin, long-press to lock a cell."
        },
        onHome = onHome,
        onLevel = onLevel,
        onRestart = { restart() },
        onHighscore = { session.openHighscore(scope) },
        extraControls = {
            IconButton(
                icon = GameIcon.Hint,
                onClick = { handleHint() },
                enabled = game.hintCount > 0 || !game.solvedAndLocked
            )
            Span({ style { hexlaserHintBadge() } }) {
                Text("${game.hintCount}")
            }
        }
    ) {
        if (session.celebrating) {
            WinConfetti(seed = level.toLong())
        }

        Canvas(attrs = {
            style { hexlaserCanvas() }
            ref { canvas ->
                canvasElement = canvas

                var activePointerId: Int? = null
                var activePosition: Position? = null
                var dragStartY = 0.0
                var dragDegrees = 0.0
                var moved = false
                var longPressTriggered = false
                var longPressJob: Job? = null

                fun pointerPoint(event: dynamic): CanvasPoint {
                    val rect = canvas.getBoundingClientRect()
                    return CanvasPoint(
                        x = (event.clientX as Number).toDouble() - rect.left,
                        y = (event.clientY as Number).toDouble() - rect.top
                    )
                }

                fun cancelLongPress() {
                    longPressJob?.cancel()
                    longPressJob = null
                }

                fun resetPointer() {
                    activePointerId = null
                    activePosition = null
                    dragDegrees = 0.0
                    moved = false
                    ongoingDrag = null
                }

                val downListener = fun(event: Event) {
                    val pointer = event.asDynamic()
                    val pointerType = pointer.pointerType as? String
                    val button = (pointer.button as? Number)?.toInt() ?: 0
                    if (pointerType == "touch" || button == 0) {
                        val point = pointerPoint(pointer)
                        val data = HexlaserDrawingData.of(
                            game.grid,
                            canvas.clientWidth.toDouble(),
                            canvas.clientHeight.toDouble()
                        )
                        val position = data.cellCloseTo(point.x, point.y)

                        if (position != null) {
                            activePointerId = (pointer.pointerId as Number).toInt()
                            activePosition = position
                            dragStartY = point.y
                            dragDegrees = 0.0
                            moved = false
                            longPressTriggered = false
                            cancelLongPress()
                            longPressJob = scope.launch {
                                delay(500)
                                if (!moved && !game.solvedAndLocked) {
                                    longPressTriggered = true
                                    beginIfNeeded()
                                    game.lock(position)
                                    version++
                                }
                            }
                            try {
                                canvas.asDynamic().setPointerCapture(pointer.pointerId)
                            } catch (_: Throwable) {
                            }
                            event.preventDefault()
                        }
                    }
                }

                val moveListener = fun(event: Event) {
                    val pointer = event.asDynamic()
                    val pointerId = (pointer.pointerId as Number).toInt()
                    if (activePointerId == pointerId) {
                        val point = pointerPoint(pointer)
                        val deltaY = point.y - dragStartY
                        if (abs(deltaY) > 8) {
                            moved = true
                            cancelLongPress()
                        }
                        dragDegrees = deltaY * 360.0 / 1000.0
                        activePosition?.let { position ->
                            ongoingDrag = position to dragDegrees
                        }
                        version++
                        event.preventDefault()
                    }
                }

                val upListener = fun(event: Event) {
                    val pointer = event.asDynamic()
                    val pointerId = (pointer.pointerId as Number).toInt()
                    if (activePointerId == pointerId) {
                        cancelLongPress()
                        val position = activePosition
                        val degrees = dragDegrees
                        val wasMoved = moved
                        val wasLongPress = longPressTriggered
                        resetPointer()
                        version++

                        try {
                            canvas.asDynamic().releasePointerCapture(pointer.pointerId)
                        } catch (_: Throwable) {
                        }

                        if (position != null && !wasLongPress) {
                            if (game.solvedAndLocked) {
                                onLevel(level + 1)
                            } else if (wasMoved && abs(degrees) >= 30.0) {
                                var positiveDegrees = degrees
                                while (positiveDegrees < 0) positiveDegrees += 360.0
                                val rotations =
                                    ((positiveDegrees + 30.0) / 60.0).toInt().coerceAtLeast(1)
                                startRotation(position, rotations)
                            } else {
                                startRotation(position, 1)
                            }
                        }
                    }
                }

                val cancelListener = fun(_: Event) {
                    cancelLongPress()
                    resetPointer()
                    version++
                }

                val resizeListener = fun(_: Event) {
                    version++
                }

                canvas.addEventListener("pointerdown", downListener)
                canvas.addEventListener("pointermove", moveListener)
                canvas.addEventListener("pointerup", upListener)
                canvas.addEventListener("pointercancel", cancelListener)
                window.addEventListener("resize", resizeListener)

                onDispose {
                    cancelLongPress()
                    canvas.removeEventListener("pointerdown", downListener)
                    canvas.removeEventListener("pointermove", moveListener)
                    canvas.removeEventListener("pointerup", upListener)
                    canvas.removeEventListener("pointercancel", cancelListener)
                    window.removeEventListener("resize", resizeListener)
                }
            }
        })

        P({ style { hexlaserHint() } }) {
            Text("Level $level · seed $seed · ${game.stageType.name.replace('_', ' ')}")
        }
    }

    GameSessionDialogs(
        session = session,
        gameTitle = gameInfo.displayName,
        stageLabel = stageLabelOf(gameInfo, stageType.wireName),
        onNextLevel = { onLevel(level + 1) },
        onStageUp = onStageUp
    )
}

private fun StyleScope.hexlaserHintBadge() {
    property("display", "inline-flex")
    property("align-items", "center")
    property("justify-content", "center")
    property("min-width", "1.6rem")
    property("height", "1.6rem")
    property("background", "linear-gradient(135deg, ${LollipopTheme.violet}, ${LollipopTheme.primary})")
    property("color", LollipopTheme.text)
    property("border-radius", "999px")
    property("font-size", "0.85rem")
    property("font-weight", LollipopTheme.fontWeightTitle)
}
package de.nielsfalk.game.suite.hexlaser

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import de.nielsfalk.game.suite.common.Score
import de.nielsfalk.game.suite.common.ScoreSyncService
import de.nielsfalk.game.suite.navigation.lollipopButton
import de.nielsfalk.game.suite.navigation.lollipopButtonSecondary
import de.nielsfalk.game.suite.navigation.lollipopCard
import de.nielsfalk.game.suite.navigation.lollipopHeading
import de.nielsfalk.game.suite.navigation.lollipopParagraph
import de.nielsfalk.game.suite.navigation.lollipopPill
import kotlinx.browser.window
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Canvas
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.events.Event
import kotlin.js.Date
import kotlin.math.abs
import kotlin.random.Random

@Composable
fun HexlaserView(
    username: String,
    stageType: StageType,
    seed: Long,
    syncService: ScoreSyncService,
    onBack: () -> Unit,
    onNewGame: (Long) -> Unit,
    onStageChange: (StageType, Long) -> Unit
) {
    val game = remember(stageType, seed) { HexlasarGame(stageType, seed) }
    val scope = rememberCoroutineScope()
    var version by remember(stageType, seed) { mutableStateOf(0) }
    var canvasElement by remember(stageType, seed) { mutableStateOf<HTMLCanvasElement?>(null) }
    var ongoingDrag by remember(stageType, seed) { mutableStateOf<Pair<Position, Double>?>(null) }
    var startedAt by remember(stageType, seed) { mutableStateOf<Double?>(null) }
    var elapsedSeconds by remember(stageType, seed) { mutableStateOf(0L) }
    var scoreSubmitted by remember(stageType, seed) { mutableStateOf(false) }
    val rotationJob = remember(stageType, seed) { mutableStateOf<Job?>(null) }

    fun beginIfNeeded() {
        if (startedAt == null) startedAt = Date.now()
    }

    fun currentElapsed(): Long =
        startedAt?.let { ((Date.now() - it) / 1000.0).toLong() } ?: 0L

    suspend fun animateWinning() {
        var spent = 0
        while (spent <= winningAnimationDuration) {
            game.setAnimationSpendTime(spent)
            version++
            delay(16)
            spent += 16
        }
        game.setAnimationSpendTime(null)
        version++
    }

    fun submitIfSolved() {
        if (!game.solved || scoreSubmitted) return
        scoreSubmitted = true
        elapsedSeconds = currentElapsed()
        scope.launch {
            syncService.recordAndPushScore(
                Score(
                    username = username,
                    gameName = "hexlaser",
                    stage = game.stageType.wireName,
                    levelId = game.seed.toString(),
                    time = elapsedSeconds,
                    points = game.points
                )
            )
        }
        scope.launch {
            animateWinning()
        }
    }

    suspend fun animateRotation(position: Position, rotations: Int) {
        val cell = game.grid[position]
        if (cell.locked || rotations == 0) return

        beginIfNeeded()
        val steps = 8
        repeat(steps) { step ->
            game.setRotatedParts(
                position,
                rotations * rotationSpeed * (step + 1) / steps
            )
            version++
            delay(18)
        }
        game.setRotatedParts(position, 0)
        game.rotate(position, rotations)
        version++
        submitIfSolved()
    }

    fun startRotation(position: Position, rotations: Int) {
        if (rotationJob.value?.isActive == true || game.solvedAndLocked) return
        rotationJob.value = scope.launch {
            animateRotation(position, rotations)
        }
    }

    fun resetSession() {
        startedAt = null
        elapsedSeconds = 0
        scoreSubmitted = false
        version++
    }

    fun handleHint() {
        if (rotationJob.value?.isActive == true || game.solvedAndLocked) return
        beginIfNeeded()
        game.requestHint()
        version++
        submitIfSolved()
    }

    LaunchedEffect(game, stageType, seed) {
        while (true) {
            delay(glowSpeed.toLong())
            if (game.followGlow()) version++
        }
    }

    LaunchedEffect(game, stageType, seed) {
        while (true) {
            delay(250)
            if (startedAt != null) {
                elapsedSeconds = currentElapsed()
            }
        }
    }

    LaunchedEffect(version, ongoingDrag, canvasElement) {
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

    Div({ style { lollipopCard() } }) {
        H2({ style { lollipopHeading() } }) {
            Text("Hexlaser · ${game.stageType.name.replace('_', ' ')}")
        }

        Div({ style { hexlaserStatusBar() } }) {
            Span({ style { lollipopPill() } }) { Text("Stage: ${game.stageType.wireName}") }
            Span({ style { lollipopPill() } }) { Text("Seed: ${game.seed}") }
            Span({ style { lollipopPill() } }) { Text("Moves: ${game.performedRotations}") }
            Span({ style { lollipopPill() } }) { Text("Time: ${elapsedSeconds}s") }
            Span({ style { lollipopPill() } }) { Text("Points: ${game.points}") }
        }

        P({ style { lollipopParagraph() } }) {
            Text(
                when {
                    game.solvedAndLocked -> "Solved! Tap the board or press Next."
                    game.stageUpVisible -> "Three solves complete."
                    else -> "Tap to rotate, drag vertically to spin, long-press to lock a cell."
                }
            )
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
                                onNewGame(Random.nextLong())
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

        Div({ style { hexlaserControls() } }) {
            Button({
                style { lollipopButtonSecondary() }
                onClick {
                    onStageChange(game.stageType.next(), Random.nextLong())
                }
            }) {
                Text("Next Stage")
            }
            Button({
                style { lollipopButtonSecondary() }
                onClick { onNewGame(Random.nextLong()) }
            }) {
                Text("Next Grid")
            }
            Button({
                style { lollipopButtonSecondary() }
                onClick {
                    game.retry()
                    resetSession()
                }
            }) {
                Text("Retry")
            }
            Button({
                style { lollipopButton() }
                onClick { handleHint() }
            }) {
                Text("Hint (${game.hintCount})")
            }
            Button({
                style { lollipopButtonSecondary() }
                onClick { onBack() }
            }) {
                Text("Back to Home")
            }
        }
    }

    if (game.stageUpVisible) {
        Div({ style { hexlaserDialogBackdrop() } }) {
            Div({ style { hexlaserDialog() } }) {
                H3({ style { lollipopHeading() } }) { Text("Stage up!") }
                P({ style { lollipopParagraph() } }) {
                    Text("Three solves in a row. Ready for the next stage?")
                }
                Div({ style { hexlaserControls() } }) {
                    Button({
                        style { lollipopButton() }
                        onClick {
                            onStageChange(game.stageType.next(), Random.nextLong())
                        }
                    }) {
                        Text("Stage up")
                    }
                    Button({
                        style { lollipopButtonSecondary() }
                        onClick { onNewGame(Random.nextLong()) }
                    }) {
                        Text("Continue")
                    }
                }
            }
        }
    }
}

private const val winningAnimationDuration = 3000

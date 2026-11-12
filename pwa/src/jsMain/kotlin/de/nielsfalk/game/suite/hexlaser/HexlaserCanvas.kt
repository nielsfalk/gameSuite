package de.nielsfalk.game.suite.hexlaser

import de.nielsfalk.game.suite.hexlaser.Direction.*
import org.w3c.dom.CanvasRenderingContext2D
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

private const val winningAnimationSpeed = 3000

private object HexColors {
    const val White = "#ffffff"
    const val Green = "#00ff00"
    const val Yellow = "#fff302"
    const val Orange = "#f88f09"
    const val Red = "#e10056"
    const val Purple = "#a818cc"
    const val Blue = "#0270dd"
    const val Black = "#000000"
}

private val usedColorsToCellColors: List<Pair<String, Set<Color>>> = listOf(
    HexColors.White to setOf(Color.Orange, Color.Green, Color.Purple),
    HexColors.Green to setOf(Color.Green),
    HexColors.Yellow to setOf(Color.Green, Color.Orange),
    HexColors.Orange to setOf(Color.Orange),
    HexColors.Red to setOf(Color.Orange, Color.Purple),
    HexColors.Purple to setOf(Color.Purple),
    HexColors.Blue to setOf(Color.Purple, Color.Green)
)

private val winningColors = usedColorsToCellColors.map { (color, _) -> color } +
        HexColors.Green + HexColors.White

private fun Set<Color>.toCanvasColor(): String? =
    usedColorsToCellColors.firstOrNull { (_, cellColors) -> cellColors == this }?.first

private fun Color.toCanvasColor(): String =
    usedColorsToCellColors.first { (_, cellColors) -> cellColors == setOf(this) }.first

data class CanvasPoint(val x: Double, val y: Double)

class HexlaserDrawingData private constructor(
    val grid: WrappedHexlaserGrid,
    val width: Double,
    val height: Double
) {
    val horizontalParts: Double = grid.x * 2 + if (grid.infiniteX) -0.6 else 1.0
    val verticalParts: Double = (if (grid.infiniteY) 1.1 else 2.5) +
            sqrt((2 * (grid.y - 1.0)).pow(2) - (grid.y - 1.0).pow(2))
    val radius: Double = min(width / horizontalParts, height / verticalParts)
    val cellHeight: Double = sqrt((2 * radius).pow(2) - radius.pow(2))
    val centerGameInitialOffset: CanvasPoint = CanvasPoint(
        x = (width - grid.x * radius * 2 + if (grid.infiniteX) radius / 2 else -radius) / 2,
        y = (height - grid.y * cellHeight + if (grid.infiniteY) radius / 1.5 else -radius) / 2
    )
    val cellOffsets: List<Pair<CanvasPoint, Cell>> =
        (0 until grid.x).flatMap { x ->
            (0 until grid.y).map { y ->
                CanvasPoint(
                    x = centerGameInitialOffset.x + (when {
                        grid.infiniteX && y.odd -> 1.2
                        grid.infiniteX -> 0.2
                        y.odd -> 2.0
                        else -> 1.0
                    }.toDouble() + x * 2) * radius,
                    y = centerGameInitialOffset.y +
                            ((if (grid.infiniteY) 0.3 else 0.7) + y) * cellHeight
                ) to grid[x, y]
            }
        }

    fun cellCloseTo(x: Double, y: Double): Position? =
        cellOffsets.minByOrNull { (offset, _) ->
            abs(offset.x - x) + abs(offset.y - y)
        }?.second?.position

    companion object {
        fun of(grid: Grid, width: Double, height: Double): HexlaserDrawingData =
            HexlaserDrawingData(grid.wrapBorderConnectionsAsCells(), width, height)
    }
}

class WrappedHexlaserGrid internal constructor(private val wrapped: Grid) {
    val infiniteX: Boolean = wrapped.infiniteX
    val infiniteY: Boolean = wrapped.infiniteY

    operator fun get(x: Int, y: Int): Cell =
        if (x < wrapped.x && y < wrapped.y) {
            wrapped[x, y]
        } else {
            wrapped[x % wrapped.x, y % wrapped.y]
        }

    val x: Int = if (infiniteX) wrapped.x + 1 else wrapped.x
    val y: Int = if (infiniteY) wrapped.y + 1 else wrapped.y
}

fun Grid.wrapBorderConnectionsAsCells(): WrappedHexlaserGrid =
    WrappedHexlaserGrid(this)

fun CanvasRenderingContext2D.drawHexlaser(
    game: HexlasarGame,
    width: Double,
    height: Double,
    ongoingDrag: Pair<Position, Double>?
) {
    fillStyle = HexColors.Black
    fillRect(0.0, 0.0, width, height)

    val data = HexlaserDrawingData.of(game.grid, width, height)
    data.cellOffsets.forEach { (center, cell) ->
        drawCell(this, cell, center, data.radius, game.grid.glowPath, ongoingDrag)
    }
    drawInfiniteBorders(data, game.grid.infiniteX, game.grid.infiniteY)
    drawWinning(this, game.animationSpendTime, width, height)
}

private fun drawCell(
    ctx: CanvasRenderingContext2D,
    cell: Cell,
    center: CanvasPoint,
    radius: Double,
    glowPath: GlowPath,
    ongoingDrag: Pair<Position, Double>?
) {
    ctx.circle(center, radius, stroke = HexColors.White, lineWidth = radius / 50)
    if (cell.locked) {
        ctx.circle(center, radius * 0.97, stroke = HexColors.White, lineWidth = radius / 25)
    }
    ctx.drawConnections(cell, center, radius, glowPath, ongoingDrag)
    ctx.drawEndpoint(cell, center, radius, glowPath)
    ctx.drawMiddlePoint(cell, center, radius, glowPath)
    ctx.drawPrisma(cell, center, radius)
    cell.source?.toCanvasColor()?.let { color ->
        ctx.circle(center, radius * 0.6, fill = color)
    }
}

private fun CanvasRenderingContext2D.drawConnections(
    cell: Cell,
    center: CanvasPoint,
    radius: Double,
    glowPath: GlowPath,
    ongoingDrag: Pair<Position, Double>?
) {
    cell.openCircleParts.forEach { (circlePart, direction) ->
        val angleOffset = -90.0 - 360.0 / 12 / 2
        val ongoingCellDrag = ongoingDrag?.let { (position, degrees) ->
            if (!cell.locked && cell.position == position) degrees else 0.0
        } ?: 0.0
        val startAngle =
            ((angleOffset + 360.0 * circlePart / 12) + cell.rotationWithParts.toDouble() * 360.0 / 6) % 360.0 +
                    ongoingCellDrag
        val middleAngle = startAngle + 360.0 / 12 / 2
        val end = plusAngle(center, middleAngle, radius)

        line(center, end, HexColors.White, radius * 0.36)
        line(center, end, HexColors.Black, radius * 0.33)

        val glowColor = if (cell.prisma) {
            glowPath[cell.position]
                .mapNotNull { it.prismaColors?.get(direction.rotate(cell.rotations)) }
                .toSet()
                .toCanvasColor()
        } else {
            glowPath.colors(cell.position).toCanvasColor()
        }
        glowColor?.let { line(center, end, it, radius * 0.1) }
    }
}

private fun CanvasRenderingContext2D.drawEndpoint(
    cell: Cell,
    center: CanvasPoint,
    radius: Double,
    glowPath: GlowPath
) {
    val color = cell.endPoint.toCanvasColor() ?: return
    val connectedColor = glowPath.colors(cell.position)
    if (connectedColor.containsAll(cell.endPoint)) {
        circle(center, radius * 0.75, stroke = HexColors.White, lineWidth = radius * 0.2)
    }
    circle(center, radius / 3, stroke = color, lineWidth = radius / 6)
}

private fun CanvasRenderingContext2D.drawMiddlePoint(
    cell: Cell,
    center: CanvasPoint,
    radius: Double,
    glowPath: GlowPath
) {
    circle(center, radius * 0.345, fill = HexColors.White)
    circle(center, radius * 0.33, fill = HexColors.Black)
    glowPath.colors(cell.position).toCanvasColor()?.let { color ->
        circle(
            center,
            radius * 0.1,
            fill = if (cell.prisma) HexColors.White else color
        )
    }
}

private fun CanvasRenderingContext2D.drawPrisma(
    cell: Cell,
    center: CanvasPoint,
    radius: Double
) {
    if (!cell.prisma) return
    circle(center, radius * 0.345, stroke = HexColors.White, lineWidth = radius / 50)

    val top = center.y - radius * 0.345
    val h = radius * 0.345 * sqrt(3.0) * sqrt(3.0) / 2
    beginPath()
    moveTo(center.x, top)
    lineTo(center.x + h / 2, center.y + h - radius * 0.345)
    lineTo(center.x - h / 2, center.y + h - radius * 0.345)
    lineTo(center.x, top)
    strokeStyle = HexColors.White
    lineWidth = radius / 50
    stroke()
}

private fun CanvasRenderingContext2D.drawInfiniteBorders(
    data: HexlaserDrawingData,
    infiniteX: Boolean,
    infiniteY: Boolean
) {
    if (!infiniteX && !infiniteY) return

    val (topLeft, _) = data.cellOffsets.first { (_, cell) -> cell.position == Position(0, 0) }
    val bottomRightX = data.cellOffsets.maxOf { it.first.x }
    val bottomRightY = data.cellOffsets.maxOf { it.first.y }

    if (infiniteX) {
        val leftLineStart = topLeft.copy(y = topLeft.y - data.radius)
        val leftLineEnd = leftLineStart.copy(y = bottomRightY + data.radius)
        fillStyle = HexColors.Black
        fillRect(0.0, 0.0, leftLineStart.x, data.height)
        line(leftLineStart, leftLineEnd, HexColors.White, data.radius / 42)

        val rightLineStart = CanvasPoint(bottomRightX, leftLineStart.y)
        val rightLineEnd = CanvasPoint(bottomRightX, leftLineEnd.y)
        fillRect(rightLineStart.x, 0.0, data.width, data.height)
        line(rightLineStart, rightLineEnd, HexColors.White, data.radius / 42)
    }

    if (infiniteY) {
        val leftLineStart = topLeft.copy(y = topLeft.y - data.radius / 2)
        val leftLineEnd = leftLineStart.copy(x = bottomRightX)
        fillStyle = HexColors.Black
        fillRect(0.0, 0.0, data.width, leftLineStart.y)
        line(leftLineStart, leftLineEnd, HexColors.White, data.radius / 42)

        val bottomLineStart = topLeft.copy(
            y = topLeft.y + data.radius + data.cellHeight * (data.grid.y - 1) - data.radius / 2
        )
        val bottomLineEnd = bottomLineStart.copy(x = bottomRightX)
        fillRect(0.0, bottomLineStart.y, data.width, data.height)
        line(bottomLineStart, bottomLineEnd, HexColors.White, data.radius / 42)
    }
}

private fun drawWinning(
    ctx: CanvasRenderingContext2D,
    animationSpendTime: Int?,
    width: Double,
    height: Double
) {
    animationSpendTime ?: return
    val percent = animationSpendTime * 100000.0 / winningAnimationSpeed / max(width, height)
    winningColors.forEachIndexed { index, color ->
        val radius = (percent - index) * 100.0
        if (radius > 0) {
            ctx.circle(
                CanvasPoint(width / 2, height / 2),
                radius,
                stroke = color,
                lineWidth = 200.0
            )
        }
    }
}

private fun CanvasRenderingContext2D.circle(
    center: CanvasPoint,
    radius: Double,
    fill: String? = null,
    stroke: String? = null,
    lineWidth: Double = 1.0
) {
    beginPath()
    arc(center.x, center.y, radius, 0.0, PI * 2)
    fill?.let {
        fillStyle = it
        fill()
    }
    stroke?.let {
        strokeStyle = it
        this.lineWidth = lineWidth
        stroke()
    }
}

private fun CanvasRenderingContext2D.line(
    start: CanvasPoint,
    end: CanvasPoint,
    color: String,
    lineWidth: Double
) {
    beginPath()
    moveTo(start.x, start.y)
    lineTo(end.x, end.y)
    strokeStyle = color
    this.lineWidth = lineWidth
    stroke()
}

private fun plusAngle(center: CanvasPoint, angle: Double, length: Double): CanvasPoint =
    CanvasPoint(
        x = center.x + cos(angle.toRadians()) * length,
        y = center.y + sin(angle.toRadians()) * length
    )

private fun Double.toRadians(): Double = this * 0.017453292

private val Cell.openCircleParts: List<Pair<Int, Direction>>
    get() = listOfNotNull(
        if (TOPRIGHT in connections) 1 to TOPRIGHT else null,
        if (RIGHT in connections) 3 to RIGHT else null,
        if (BOTTOMRIGHT in connections) 5 to BOTTOMRIGHT else null,
        if (BOTTOMLEFT in connections) 7 to BOTTOMLEFT else null,
        if (LEFT in connections) 9 to LEFT else null,
        if (TOPLEFT in connections) 11 to TOPLEFT else null
    )

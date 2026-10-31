package de.nielsfalk.game.suite.minesweeper

import de.nielsfalk.game.suite.navigation.LollipopTheme
import org.jetbrains.compose.web.css.StyleScope

fun StyleScope.minesweeperBoard(width: Int) {
    property("display", "grid")
    property("grid-template-columns", "repeat($width, minmax(0, 1fr))")
    property("gap", "3px")
    property("max-width", "min(100%, ${width * 44}px)")
    property("margin", "0 auto 1rem auto")
    property("padding", "6px")
    property("background", "rgba(0,0,0,0.28)")
    property("border-radius", "18px")
    property("box-shadow", "inset 0 0 0 1px rgba(255,255,255,0.06), ${LollipopTheme.shadow}")
    property("touch-action", "manipulation")
}

fun StyleScope.minesweeperCell(cell: MinesweeperCell, status: MinesweeperStatus) {
    property("aspect-ratio", "1")
    property("display", "flex")
    property("align-items", "center")
    property("justify-content", "center")
    property("border-radius", "7px")
    property("font-family", LollipopTheme.fontFamily)
    property("font-weight", LollipopTheme.fontWeightTitle)
    property("font-size", "clamp(0.65rem, 2.2vw, 1.05rem)")
    property("line-height", "1")
    property("user-select", "none")
    property("-webkit-user-select", "none")
    property("cursor", "pointer")
    property("transition", "filter .1s ease, transform .05s ease")

    when {
        cell.revealed && cell.hasMine -> {
            property("background", "linear-gradient(135deg, ${LollipopTheme.primary}, #b3245c)")
            property("color", LollipopTheme.text)
            property("box-shadow", "inset 0 0 12px rgba(255,77,141,0.6)")
        }

        cell.revealed -> {
            property("background", "#151534")
            property("color", minesweeperNumberColor(cell.adjacentMines))
            property("box-shadow", "inset 0 1px 2px rgba(0,0,0,0.5)")
        }

        cell.flagged -> {
            property("background", "linear-gradient(135deg, ${LollipopTheme.accent}, ${LollipopTheme.orange})")
            property("color", LollipopTheme.background)
            property("box-shadow", "0 3px 0 rgba(0,0,0,0.35)")
        }

        status == MinesweeperStatus.Lost -> {
            property("background", "linear-gradient(145deg, ${LollipopTheme.surfaceAlt}, #23235a)")
            property("color", "transparent")
            property("opacity", "0.65")
        }

        else -> {
            property("background", "linear-gradient(145deg, ${LollipopTheme.surfaceAlt}, #23235a)")
            property("color", "transparent")
            property("box-shadow", "0 3px 0 rgba(0,0,0,0.35), inset 0 1px 0 rgba(255,255,255,0.12)")
        }
    }
}

fun StyleScope.minesweeperStatusBar(version: Int) {
    property("display", "flex")
    property("flex-wrap", "wrap")
    property("gap", "0.5rem")
    property("align-items", "center")
    property("justify-content", "center")
    property("margin-bottom", "1rem")
    property("--minesweeper-version", version.toString())
}

fun StyleScope.minesweeperControls() {
    property("display", "flex")
    property("flex-wrap", "wrap")
    property("gap", "0.6rem")
    property("align-items", "center")
    property("justify-content", "center")
    property("margin-top", "0.5rem")
}

fun StyleScope.minesweeperHint() {
    property("color", LollipopTheme.textMuted)
    property("font-size", "0.95rem")
    property("text-align", "center")
    property("margin", "0.5rem 0 0 0")
}

fun minesweeperNumberColor(count: Int): String = when (count) {
    1 -> LollipopTheme.secondary
    2 -> LollipopTheme.success
    3 -> LollipopTheme.primary
    4 -> LollipopTheme.violet
    5 -> LollipopTheme.orange
    6 -> LollipopTheme.accent
    7 -> "#ff7ad9"
    8 -> LollipopTheme.textMuted
    else -> LollipopTheme.text
}

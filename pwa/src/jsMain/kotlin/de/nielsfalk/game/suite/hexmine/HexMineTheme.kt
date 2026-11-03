package de.nielsfalk.game.suite.hexmine

import de.nielsfalk.game.suite.minesweeper.MinesweeperStatus
import de.nielsfalk.game.suite.minesweeper.minesweeperNumberColor
import de.nielsfalk.game.suite.navigation.LollipopTheme
import org.jetbrains.compose.web.css.StyleScope

fun StyleScope.hexMineBoardScroller() {
    property("max-width", "100%")
    property("overflow-x", "auto")
    property("padding-bottom", "4px")
}

fun StyleScope.hexMineBoard(width: Int) {
    property("--hex-size", "clamp(12px, calc((100vw - 160px) / $width), 38px)")
    property("display", "flex")
    property("flex-direction", "column")
    property("align-items", "flex-start")
    property("width", "fit-content")
    property("margin", "0 auto 1rem auto")
    property("padding", "10px")
    property("background", "rgba(0,0,0,0.28)")
    property("border-radius", "18px")
    property("box-shadow", "inset 0 0 0 1px rgba(255,255,255,0.06), ${LollipopTheme.shadow}")
    property("touch-action", "manipulation")
}

fun StyleScope.hexMineRow(row: Int) {
    property("display", "flex")
    property("gap", "2px")
    if (row % 2 == 1) {
        property("margin-left", "calc(var(--hex-size) / 2 + 1px)")
    }
    if (row > 0) {
        property("margin-top", "calc(var(--hex-size) * -0.18)")
    }
}

fun StyleScope.hexMineCell(cell: HexMineCell, status: MinesweeperStatus) {
    property("width", "var(--hex-size)")
    property("height", "calc(var(--hex-size) * 1.12)")
    property("clip-path", "polygon(50% 0%, 100% 25%, 100% 75%, 50% 100%, 0% 75%, 0% 25%)")
    property("display", "flex")
    property("align-items", "center")
    property("justify-content", "center")
    property("font-family", LollipopTheme.fontFamily)
    property("font-weight", LollipopTheme.fontWeightTitle)
    property("font-size", "clamp(0.55rem, 1.8vw, 0.95rem)")
    property("line-height", "1")
    property("user-select", "none")
    property("-webkit-user-select", "none")
    property("cursor", "pointer")
    property("transition", "filter .1s ease, transform .05s ease")

    when {
        cell.revealed && cell.hasMine -> {
            property("background", "linear-gradient(135deg, ${LollipopTheme.primary}, #b3245c)")
            property("color", LollipopTheme.text)
        }

        cell.revealed -> {
            property("background", "#151534")
            property("color", minesweeperNumberColor(cell.adjacentMines))
        }

        cell.flagged -> {
            property("background", "linear-gradient(135deg, ${LollipopTheme.accent}, ${LollipopTheme.orange})")
            property("color", LollipopTheme.background)
        }

        status == MinesweeperStatus.Lost -> {
            property("background", "linear-gradient(145deg, ${LollipopTheme.surfaceAlt}, #23235a)")
            property("color", "transparent")
            property("opacity", "0.65")
        }

        else -> {
            property("background", "linear-gradient(145deg, ${LollipopTheme.surfaceAlt}, #23235a)")
            property("color", "transparent")
        }
    }
}

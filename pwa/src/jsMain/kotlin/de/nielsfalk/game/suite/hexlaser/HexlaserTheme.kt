package de.nielsfalk.game.suite.hexlaser

import de.nielsfalk.game.suite.navigation.LollipopTheme
import org.jetbrains.compose.web.css.StyleScope
import org.jetbrains.compose.web.dom.P

fun StyleScope.hexlaserStatusBar() {
    property("display", "flex")
    property("flex-wrap", "wrap")
    property("gap", "0.35rem")
    property("margin", "0.5rem 0 1rem 0")
}

fun StyleScope.hexlaserCanvas() {
    property("display", "block")
    property("width", "100%")
    property("height", "min(68vh, 620px)")
    property("min-height", "320px")
    property("background", "#000000")
    property("border", "2px solid rgba(77,208,255,0.35)")
    property("border-radius", LollipopTheme.radiusSmall)
    property("box-shadow", "inset 0 0 28px rgba(0,0,0,0.8), 0 0 24px rgba(77,208,255,0.18)")
    property("touch-action", "none")
    property("user-select", "none")
    property("cursor", "pointer")
}

fun StyleScope.hexlaserHint() {
    property("color", LollipopTheme.textMuted)
    property("font-size", "0.95rem")
    property("text-align", "center")
    property("margin", "0.5rem 0 0 0")
}
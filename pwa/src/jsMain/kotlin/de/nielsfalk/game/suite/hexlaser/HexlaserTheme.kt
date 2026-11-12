package de.nielsfalk.game.suite.hexlaser

import de.nielsfalk.game.suite.navigation.LollipopTheme
import org.jetbrains.compose.web.css.StyleScope

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

fun StyleScope.hexlaserControls() {
    property("display", "flex")
    property("flex-wrap", "wrap")
    property("gap", "0.6rem")
    property("margin-top", "1rem")
}

fun StyleScope.hexlaserDialogBackdrop() {
    property("position", "fixed")
    property("inset", "0")
    property("background", "rgba(5,5,20,0.72)")
    property("display", "flex")
    property("align-items", "center")
    property("justify-content", "center")
    property("padding", "1rem")
    property("z-index", "20")
}

fun StyleScope.hexlaserDialog() {
    property("background", "linear-gradient(160deg, #2e2e70 0%, ${LollipopTheme.surface} 100%)")
    property("border", "2px solid rgba(255,210,63,0.55)")
    property("border-radius", LollipopTheme.radius)
    property("padding", "1.5rem")
    property("max-width", "420px")
    property("width", "100%")
    property("box-shadow", LollipopTheme.shadow)
    property("text-align", "center")
}

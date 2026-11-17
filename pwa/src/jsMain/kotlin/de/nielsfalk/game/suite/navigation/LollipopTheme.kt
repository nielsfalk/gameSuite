package de.nielsfalk.game.suite.navigation

import org.jetbrains.compose.web.css.StyleScope

/**
 * Lollipop design tokens: fat font, rounded edges, dark background, and popping colors.
 */
object LollipopTheme {
    const val background = "#12122b"
    const val surface = "#1f1f4a"
    const val surfaceAlt = "#2a2a66"
    const val primary = "#ff4d8d"
    const val secondary = "#4dd0ff"
    const val accent = "#ffd23f"
    const val success = "#7cfc8b"
    const val violet = "#b388ff"
    const val orange = "#ff9f43"
    const val text = "#ffffff"
    const val textMuted = "#c9c9e8"

    const val fontFamily = "'Baloo 2', 'Segoe UI', system-ui, sans-serif"
    const val fontWeight = "800"
    const val fontWeightTitle = "900"

    const val radius = "28px"
    const val radiusSmall = "16px"
    const val radiusPill = "999px"

    const val shadow = "0 12px 32px rgba(0,0,0,0.45)"
    const val shadowPop = "0 6px 0 rgba(0,0,0,0.35)"
}

fun StyleScope.lollipopRoot() {
    property("font-family", LollipopTheme.fontFamily)
    property("font-weight", LollipopTheme.fontWeight)
    property("background", "radial-gradient(circle at 15% 0%, ${LollipopTheme.surfaceAlt} 0%, ${LollipopTheme.background} 55%)")
    property("background-attachment", "fixed")
    property("color", LollipopTheme.text)
    property("margin", "0 auto")
    property("max-width", "860px")
    property("min-height", "100vh")
    property("padding", "1.5rem")
    property("box-sizing", "border-box")
}

fun StyleScope.lollipopCard() {
    property("background", "linear-gradient(160deg, #262659 0%, ${LollipopTheme.surface} 100%)")
    property("border", "2px solid rgba(179,136,255,0.18)")
    property("border-radius", LollipopTheme.radius)
    property("padding", "1.5rem")
    property("box-sizing", "border-box")
    property("box-shadow", "${LollipopTheme.shadow}, 0 0 0 1px rgba(255,255,255,0.04) inset")
    property("margin-bottom", "1.25rem")
    property("position", "relative")
    property("overflow", "hidden")
}

fun StyleScope.lollipopTitle() {
    property("font-weight", LollipopTheme.fontWeightTitle)
    property("font-size", "2.7rem")
    property("letter-spacing", "0.5px")
    property("margin", "0 0 0.75rem 0")
    property("background", "linear-gradient(90deg, ${LollipopTheme.accent} 0%, ${LollipopTheme.primary} 45%, ${LollipopTheme.secondary} 100%)")
    property("-webkit-background-clip", "text")
    property("background-clip", "text")
    property("color", "transparent")
    property("-webkit-text-fill-color", "transparent")
    property("filter", "drop-shadow(0 2px 10px rgba(255,77,141,0.45))")
}

fun StyleScope.lollipopHeading() {
    property("font-weight", LollipopTheme.fontWeightTitle)
    property("color", LollipopTheme.secondary)
    property("font-size", "1.7rem")
    property("margin", "0 0 0.75rem 0")
    property("text-shadow", "0 0 18px rgba(77,208,255,0.45)")
}

fun StyleScope.lollipopSubheading() {
    property("font-weight", LollipopTheme.fontWeight)
    property("color", LollipopTheme.primary)
    property("font-size", "1.2rem")
    property("margin", "1.25rem 0 0.5rem 0")
}

fun StyleScope.lollipopParagraph() {
    property("color", LollipopTheme.textMuted)
    property("font-size", "1.05rem")
    property("margin", "0.25rem 0 0.75rem 0")
}

fun StyleScope.lollipopInput() {
    property("background", LollipopTheme.surfaceAlt)
    property("color", LollipopTheme.text)
    property("border", "2px solid rgba(77,208,255,0.35)")
    property("border-radius", LollipopTheme.radiusSmall)
    property("padding", "0.75rem 1rem")
    property("font-family", LollipopTheme.fontFamily)
    property("font-weight", LollipopTheme.fontWeight)
    property("font-size", "1rem")
    property("caret-color", LollipopTheme.accent)
    property("transition", "border-color .15s ease, box-shadow .15s ease")
}

fun StyleScope.lollipopButton() {
    property("background", "linear-gradient(135deg, ${LollipopTheme.primary} 0%, ${LollipopTheme.violet} 100%)")
    property("color", LollipopTheme.text)
    property("border", "none")
    property("border-radius", LollipopTheme.radiusPill)
    property("padding", "0.85rem 1.8rem")
    property("font-family", LollipopTheme.fontFamily)
    property("font-weight", LollipopTheme.fontWeightTitle)
    property("font-size", "1.1rem")
    property("letter-spacing", "0.5px")
    property("box-shadow", "${LollipopTheme.shadowPop}, 0 0 20px rgba(255,77,141,0.45)")
    property("cursor", "pointer")
    property("transition", "transform .12s ease, filter .12s ease")
}

fun StyleScope.lollipopButtonSecondary() {
    property("background", "linear-gradient(135deg, ${LollipopTheme.secondary} 0%, ${LollipopTheme.success} 100%)")
    property("color", LollipopTheme.background)
    property("border", "none")
    property("border-radius", LollipopTheme.radiusPill)
    property("padding", "0.85rem 1.8rem")
    property("font-family", LollipopTheme.fontFamily)
    property("font-weight", LollipopTheme.fontWeightTitle)
    property("font-size", "1.1rem")
    property("letter-spacing", "0.5px")
    property("box-shadow", "${LollipopTheme.shadowPop}, 0 0 20px rgba(77,208,255,0.45)")
    property("cursor", "pointer")
    property("transition", "transform .12s ease, filter .12s ease")
}

fun StyleScope.lollipopPill() {
    property("display", "inline-block")
    property("background", "linear-gradient(135deg, rgba(255,210,63,0.18), rgba(255,77,141,0.18))")
    property("border", "1px solid rgba(255,210,63,0.45)")
    property("color", LollipopTheme.accent)
    property("border-radius", LollipopTheme.radiusPill)
    property("padding", "0.35rem 0.9rem")
    property("font-weight", LollipopTheme.fontWeight)
    property("margin", "0.15rem")
}

fun StyleScope.lollipopScoreRow() {
    property("background", "linear-gradient(90deg, ${LollipopTheme.surfaceAlt} 0%, ${LollipopTheme.surface} 100%)")
    property("border-left", "4px solid ${LollipopTheme.primary}")
    property("border-radius", LollipopTheme.radiusSmall)
    property("padding", "0.6rem 1rem")
    property("margin", "0.4rem 0")
    property("color", LollipopTheme.text)
}

fun StyleScope.lollipopIconButton(enabled: Boolean = true, active: Boolean = false) {
    property("display", "inline-flex")
    property("align-items", "center")
    property("justify-content", "center")
    property("width", "2.9rem")
    property("height", "2.9rem")
    property("padding", "0")
    property("border", if (active) "2px solid ${LollipopTheme.accent}" else "2px solid rgba(255,255,255,0.12)")
    property(
        "background",
        when {
            !enabled -> "rgba(42,42,102,0.55)"
            active -> "linear-gradient(135deg, ${LollipopTheme.accent} 0%, ${LollipopTheme.orange} 100%)"
            else -> "linear-gradient(135deg, ${LollipopTheme.surfaceAlt} 0%, ${LollipopTheme.surface} 100%)"
        }
    )
    property("color", when {
        !enabled -> "rgba(201,201,232,0.4)"
        active -> LollipopTheme.background
        else -> LollipopTheme.secondary
    })
    property("border-radius", LollipopTheme.radiusPill)
    property("box-shadow", if (enabled) LollipopTheme.shadowPop else "none")
    property("cursor", if (enabled) "pointer" else "not-allowed")
    property("opacity", if (enabled) "1" else "0.6")
    property("transition", "transform .12s ease, filter .12s ease")
}

fun StyleScope.lollipopToolbar() {
    property("display", "flex")
    property("align-items", "center")
    property("justify-content", "center")
    property("flex-wrap", "wrap")
    property("gap", "0.35rem")
    property("margin", "0.75rem 0")
}

fun StyleScope.lollipopToolbarSpacer() {
    property("flex", "1 1 auto")
    property("min-width", "0.25rem")
}

fun StyleScope.lollipopDialogBackdrop() {
    property("position", "fixed")
    property("inset", "0")
    property("background", "rgba(9,9,24,0.72)")
    property("backdrop-filter", "blur(3px)")
    property("display", "flex")
    property("align-items", "center")
    property("justify-content", "center")
    property("padding", "1rem")
    property("z-index", "50")
}

fun StyleScope.lollipopDialogCard() {
    property("background", "linear-gradient(160deg, #2b2b6b 0%, ${LollipopTheme.surface} 100%)")
    property("border", "2px solid rgba(255,210,63,0.35)")
    property("border-radius", LollipopTheme.radius)
    property("padding", "1.5rem")
    property("width", "100%")
    property("max-width", "30rem")
    property("max-height", "85vh")
    property("overflow-y", "auto")
    property("box-sizing", "border-box")
    property("box-shadow", LollipopTheme.shadow)
}

fun StyleScope.lollipopLogoGrid() {
    property("display", "grid")
    property("grid-template-columns", "repeat(auto-fit, minmax(12rem, 1fr))")
    property("gap", "1rem")
    property("margin", "1rem 0 1.5rem 0")
}

fun StyleScope.lollipopLogoCard() {
    property("display", "flex")
    property("flex-direction", "column")
    property("align-items", "center")
    property("gap", "0.5rem")
    property("padding", "1.25rem 1rem")
    property("background", "linear-gradient(160deg, ${LollipopTheme.surfaceAlt} 0%, ${LollipopTheme.surface} 100%)")
    property("border", "2px solid rgba(77,208,255,0.28)")
    property("border-radius", LollipopTheme.radius)
    property("box-shadow", LollipopTheme.shadowPop)
    property("cursor", "pointer")
    property("transition", "transform .12s ease, border-color .12s ease")
    property("text-align", "center")
}

fun StyleScope.lollipopSummaryRow() {
    property("background", "linear-gradient(90deg, rgba(77,208,255,0.12) 0%, rgba(42,42,102,0.35) 100%)")
    property("border-left", "4px solid ${LollipopTheme.secondary}")
    property("border-radius", LollipopTheme.radiusSmall)
    property("padding", "0.6rem 0.9rem")
    property("margin", "0.4rem 0")
    property("color", LollipopTheme.text)
}

fun StyleScope.lollipopHighscoreRow(isOwn: Boolean = false) {
    property("display", "flex")
    property("align-items", "center")
    property("gap", "0.6rem")
    property("background", if (isOwn) "linear-gradient(90deg, rgba(255,210,63,0.25) 0%, rgba(255,77,141,0.15) 100%)" else "rgba(42,42,102,0.45)")
    property("border", if (isOwn) "2px solid ${LollipopTheme.accent}" else "1px solid rgba(255,255,255,0.08)")
    property("border-radius", LollipopTheme.radiusSmall)
    property("padding", "0.45rem 0.8rem")
    property("margin", "0.3rem 0")
    property("color", LollipopTheme.text)
}

fun StyleScope.lollipopMutedText() {
    property("color", LollipopTheme.textMuted)
    property("font-size", "0.95rem")
}

fun StyleScope.lollipopLevelPill() {
    property("display", "inline-block")
    property("background", "linear-gradient(135deg, ${LollipopTheme.primary} 0%, ${LollipopTheme.violet} 100%)")
    property("color", LollipopTheme.text)
    property("border-radius", LollipopTheme.radiusPill)
    property("padding", "0.25rem 0.9rem")
    property("font-weight", LollipopTheme.fontWeightTitle)
    property("letter-spacing", "0.5px")
}

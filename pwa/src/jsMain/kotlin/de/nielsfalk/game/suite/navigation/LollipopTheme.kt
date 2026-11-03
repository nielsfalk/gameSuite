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

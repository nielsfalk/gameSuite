package de.nielsfalk.game.suite.navigation

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
    const val success = "#7CFC8B"
    const val text = "#ffffff"
    const val textMuted = "#c9c9e8"

    const val fontFamily = "'Baloo 2', 'Segoe UI', system-ui, sans-serif"
    const val fontWeight = "800"
    const val fontWeightTitle = "900"

    const val radius = "24px"
    const val radiusSmall = "14px"
    const val radiusPill = "999px"

    const val shadow = "0 8px 24px rgba(0,0,0,0.45)"
    const val shadowPop = "0 6px 0 rgba(0,0,0,0.35)"

    fun rootContainer() =
        "font-family:$fontFamily;" +
            "font-weight:$fontWeight;" +
            "background:$background;" +
            "color:$text;" +
            "margin:0 auto;" +
            "max-width:860px;" +
            "min-height:100vh;" +
            "padding:1.5rem;"

    fun card() =
        "background:$surface;" +
            "border-radius:$radius;" +
            "padding:1.5rem;" +
            "box-shadow:$shadow;" +
            "margin-bottom:1.25rem;"

    fun title() =
        "font-weight:$fontWeightTitle;" +
            "color:$accent;" +
            "font-size:2.4rem;" +
            "margin:0 0 0.5rem 0;"

    fun heading() =
        "font-weight:$fontWeightTitle;" +
            "color:$secondary;" +
            "font-size:1.6rem;" +
            "margin:0 0 0.75rem 0;"

    fun subheading() =
        "font-weight:$fontWeight;" +
            "color:$primary;" +
            "font-size:1.2rem;" +
            "margin:0 0 0.5rem 0;"

    fun paragraph() =
        "color:$textMuted;" +
            "font-size:1.05rem;" +
            "margin:0.25rem 0;"

    fun input() =
        "background:$surfaceAlt;" +
            "color:$text;" +
            "border:none;" +
            "border-radius:$radiusSmall;" +
            "padding:0.75rem 1rem;" +
            "font-family:$fontFamily;" +
            "font-weight:$fontWeight;" +
            "font-size:1rem;"

    fun button() =
        "background:$primary;" +
            "color:$text;" +
            "border:none;" +
            "border-radius:$radiusPill;" +
            "padding:0.8rem 1.6rem;" +
            "font-family:$fontFamily;" +
            "font-weight:$fontWeightTitle;" +
            "font-size:1.1rem;" +
            "box-shadow:$shadowPop;" +
            "cursor:pointer;"

    fun buttonSecondary() =
        "background:$secondary;" +
            "color:$background;" +
            "border:none;" +
            "border-radius:$radiusPill;" +
            "padding:0.8rem 1.6rem;" +
            "font-family:$fontFamily;" +
            "font-weight:$fontWeightTitle;" +
            "font-size:1.1rem;" +
            "box-shadow:$shadowPop;" +
            "cursor:pointer;"

    fun pill() =
        "display:inline-block;" +
            "background:$surfaceAlt;" +
            "color:$accent;" +
            "border-radius:$radiusPill;" +
            "padding:0.35rem 0.9rem;" +
            "font-weight:$fontWeight;" +
            "margin:0.15rem;"

    fun scoreRow() =
        "background:$surfaceAlt;" +
            "border-radius:$radiusSmall;" +
            "padding:0.6rem 1rem;" +
            "margin:0.4rem 0;" +
            "color:$text;"
}
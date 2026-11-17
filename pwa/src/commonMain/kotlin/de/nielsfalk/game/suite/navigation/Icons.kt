package de.nielsfalk.game.suite.navigation

/**
 * Inline SVG icons of the game shell.
 *
 * The markup is generated as a plain string so that it can be rendered with a single element and
 * asserted in ordinary unit tests.
 */
enum class GameIcon(val label: String) {
    Home("Home"),
    Back10("10 levels back"),
    Back1("1 level back"),
    Refresh("Restart level"),
    Forward1("1 level forward"),
    Forward10("10 levels forward"),
    Trophy("Highscore of this level"),
    Flag("Flag mode"),
    Hint("Hint"),
    Star("Best"),
    Close("Close")
}

object Icons {
    private val paths: Map<GameIcon, String> = mapOf(
        GameIcon.Home to
            "<path d='M3 10.5 12 3l9 7.5'/><path d='M5.5 9.5V21h13V9.5'/><path d='M10 21v-6h4v6'/>",
        GameIcon.Back1 to "<path d='M15 4.5 7.5 12 15 19.5'/>",
        GameIcon.Back10 to "<path d='M13 4.5 5.5 12 13 19.5'/><path d='M20 4.5 12.5 12 20 19.5'/>",
        GameIcon.Forward1 to "<path d='M9 4.5 16.5 12 9 19.5'/>",
        GameIcon.Forward10 to "<path d='M4 4.5 11.5 12 4 19.5'/><path d='M11 4.5 18.5 12 11 19.5'/>",
        GameIcon.Refresh to
            "<path d='M3 12a9 9 0 0 1 15.5-6.2L21 8'/><path d='M21 3v5h-5'/>" +
                "<path d='M21 12a9 9 0 0 1-15.5 6.2L3 16'/><path d='M3 21v-5h5'/>",
        GameIcon.Trophy to
            "<path d='M8 4h8v5a4 4 0 0 1-8 0z'/><path d='M8 6H4.5v1.5A3.5 3.5 0 0 0 8 11'/>" +
                "<path d='M16 6h3.5v1.5A3.5 3.5 0 0 1 16 11'/><path d='M12 13v5'/>" +
                "<path d='M8.5 21h7'/><path d='M10 18h4'/>",
        GameIcon.Flag to "<path d='M6 3v18'/><path d='M6 4h11l-3 4 3 4H6'/>",
        GameIcon.Hint to
            "<path d='M12 3a6 6 0 0 0-3.8 10.6c.9.8 1.3 1.6 1.3 2.9h5c0-1.3.4-2.1 1.3-2.9A6 6 0 0 0 12 3z'/>" +
                "<path d='M9.5 19.5h5'/><path d='M10.5 22h3'/>",
        GameIcon.Star to
            "<path d='M12 3.5l2.7 5.5 6 .9-4.3 4.2 1 6-5.4-2.8-5.4 2.8 1-6L3.3 9.9l6-.9z'/>",
        GameIcon.Close to "<path d='M6 6l12 12'/><path d='M18 6 6 18'/>"
    )

    /** The inline SVG markup of [icon]. */
    fun markup(icon: GameIcon, sizePx: Int = 22): String {
        val body = paths[icon] ?: ""
        return "<svg viewBox='0 0 24 24' width='$sizePx' height='$sizePx' fill='none' " +
            "stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round' " +
            "aria-hidden='true' focusable='false'>$body</svg>"
    }
}

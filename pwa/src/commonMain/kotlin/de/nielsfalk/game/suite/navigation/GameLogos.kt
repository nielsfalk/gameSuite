package de.nielsfalk.game.suite.navigation

/**
 * Inline SVG logos of the games, shown on the user home.
 *
 * Every logo is a plain string so that it can be rendered with a single element, scales without
 * raster assets and is asserted in unit tests.
 */
object GameLogos {
    private val markups: Map<String, String> = mapOf(
        "minesweeper" to
            // Rounded tile with a mine and a four-way blast
            "<rect x='6' y='6' width='52' height='52' rx='14' fill='#2a2a66' stroke='#4dd0ff' stroke-width='3'/>" +
                "<circle cx='32' cy='32' r='10' fill='#ff4d8d'/>" +
                "<g stroke='#ffd23f' stroke-width='4' stroke-linecap='round'>" +
                "<path d='M32 12v6'/><path d='M32 46v6'/><path d='M12 32h6'/><path d='M46 32h6'/>" +
                "<path d='M18 18l4 4'/><path d='M42 42l4 4'/><path d='M46 18l-4 4'/><path d='M22 42l-4 4'/>" +
                "</g>",
        "hexmine" to
            // Two hexagon cells with a mine in the upper one
            "<path d='M32 6 54 19v26L32 58 10 45V19z' fill='#2a2a66' stroke='#7cfc8b' stroke-width='3'/>" +
                "<circle cx='32' cy='26' r='8' fill='#ff4d8d'/>" +
                "<g stroke='#ffd23f' stroke-width='3' stroke-linecap='round'>" +
                "<path d='M32 15v4'/><path d='M32 33v4'/><path d='M22 26h4'/><path d='M38 26h4'/>" +
                "</g>" +
                "<path d='M22 48l6-3'/><path d='M36 45l6 3'/>",
        "hexlaser" to
            // Hexagon with a glowing beam through its center
            "<path d='M32 6 54 19v26L32 58 10 45V19z' fill='#2a2a66' stroke='#b388ff' stroke-width='3'/>" +
                "<g stroke='#ffd23f' stroke-width='5' stroke-linecap='round'>" +
                "<path d='M20 42 44 22'/>" +
                "</g>" +
                "<circle cx='32' cy='32' r='5' fill='#4dd0ff'/>" +
                "<path d='M26 32h12' stroke='#ff4d8d' stroke-width='2'/>"
    )

    /** The inline SVG markup of the logo of [gameName]. */
    fun markup(gameName: String, sizePx: Int = 64): String {
        val body = markups[gameName] ?: fallback
        return "<svg viewBox='0 0 64 64' width='$sizePx' height='$sizePx' role='img' " +
            "aria-label='$gameName logo'>$body</svg>"
    }

    /** Whether a hand-drawn logo exists for [gameName]. */
    fun hasLogo(gameName: String): Boolean = markups.containsKey(gameName)

    private const val fallback: String =
        "<rect x='6' y='6' width='52' height='52' rx='14' fill='#2a2a66' stroke='#c9c9e8' stroke-width='3'/>" +
            "<path d='M32 18v28M18 32h28' stroke='#c9c9e8' stroke-width='4' stroke-linecap='round'/>"
}

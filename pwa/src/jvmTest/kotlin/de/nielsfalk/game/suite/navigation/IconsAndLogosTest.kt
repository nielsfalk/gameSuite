package de.nielsfalk.game.suite.navigation

import io.kotest.core.spec.style.FreeSpec

class IconsAndLogosTest : FreeSpec({
    "Icons" - {
        "renders an inline svg for every icon" - {
            GameIcon.entries.forEach { icon ->
                icon.name {
                    val markup = Icons.markup(icon)
                    assert(markup.startsWith("<svg"))
                    assert(markup.endsWith("</svg>"))
                    assert(markup.contains("stroke='currentColor'"))
                    assert(markup.contains("width='22'"))
                    assert(icon.label.isNotBlank())
                }
            }
        }

        "renders distinct markup per icon" {
            val markups = GameIcon.entries.map { Icons.markup(it) }
            assert(markups.toSet().size == markups.size)
        }

        "respects the requested size" {
            assert(Icons.markup(GameIcon.Home, sizePx = 48).contains("width='48'"))
            assert(Icons.markup(GameIcon.Home, sizePx = 48).contains("height='48'"))
        }

        "has no script in its markup" {
            GameIcon.entries.forEach { icon ->
                assert(!Icons.markup(icon).contains("<script", ignoreCase = true))
            }
        }
    }

    "GameLogos" - {
        "renders a logo for every catalogued game" - {
            de.nielsfalk.game.suite.common.GameCatalog.games.forEach { game ->
                game.name {
                    assert(GameLogos.hasLogo(game.name))
                    val markup = GameLogos.markup(game.name)
                    assert(markup.startsWith("<svg"))
                    assert(markup.contains("aria-label='${game.name} logo'"))
                }
            }
        }

        "falls back for unknown games" {
            assert(!GameLogos.hasLogo("unknown"))
            assert(GameLogos.markup("unknown").startsWith("<svg"))
        }

        "renders distinct logos per game" {
            val markups = de.nielsfalk.game.suite.common.GameCatalog.games.map {
                GameLogos.markup(it.name)
            }
            assert(markups.toSet().size == markups.size)
        }

        "respects the requested size" {
            assert(GameLogos.markup("minesweeper", sizePx = 96).contains("width='96'"))
        }
    }
})

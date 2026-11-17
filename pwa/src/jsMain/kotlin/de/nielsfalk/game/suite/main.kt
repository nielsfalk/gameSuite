package de.nielsfalk.game.suite

import de.nielsfalk.game.suite.common.BrowserLocalStorageDriver
import de.nielsfalk.game.suite.common.ScoreStorageService
import de.nielsfalk.game.suite.common.ScoreSyncService
import de.nielsfalk.game.suite.navigation.AppView
import kotlinx.browser.document
import org.jetbrains.compose.web.renderComposable

fun main() {
    // Hydrate inline SVG icons: replace every .gamesuite-icon-slot with its innerHTML as the SVG markup
    try {
        val nodes = document.querySelectorAll(".gamesuite-icon-slot")
        for (i in 0 until nodes.length) {
            val el = nodes.item(i).asDynamic()
            val html: String = el.innerHTML as String
            if (html.startsWith("<svg")) {
                el.innerHTML = html
            }
        }
    } catch (_: Throwable) {
    }

    val storageService = ScoreStorageService(BrowserLocalStorageDriver())
    val syncService = ScoreSyncService(storageService)

    renderComposable(rootElementId = "root") {
        AppView(
            storageService = storageService,
            syncService = syncService
        )
    }
}

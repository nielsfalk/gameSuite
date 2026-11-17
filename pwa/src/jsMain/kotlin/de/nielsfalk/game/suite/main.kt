package de.nielsfalk.game.suite

import de.nielsfalk.game.suite.common.BrowserLocalStorageDriver
import de.nielsfalk.game.suite.common.ScoreStorageService
import de.nielsfalk.game.suite.common.ScoreSyncService
import de.nielsfalk.game.suite.navigation.AppView
import org.jetbrains.compose.web.renderComposable

fun main() {
    val storageService = ScoreStorageService(BrowserLocalStorageDriver())
    val syncService = ScoreSyncService(storageService)

    renderComposable(rootElementId = "root") {
        AppView(
            storageService = storageService,
            syncService = syncService
        )
    }
}

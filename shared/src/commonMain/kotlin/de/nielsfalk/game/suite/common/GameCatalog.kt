package de.nielsfalk.game.suite.common

import de.nielsfalk.game.suite.hexlaser.StageType
import de.nielsfalk.game.suite.minesweeper.MinesweeperStage

/**
 * Source of truth for games and stages the platform accepts.
 *
 * The server uses this to reject score requests for unknown games or stages.
 */
object GameCatalog {
    const val MINESWEEPER: String = "minesweeper"
    const val HEXMINE: String = "hexmine"
    const val HEXLASER: String = "hexlaser"

    val gameNames: Set<String> = setOf(MINESWEEPER, HEXMINE, HEXLASER)

    fun isValidGame(gameName: String): Boolean = gameName in gameNames

    fun isValidStage(gameName: String, stage: String): Boolean = when (gameName) {
        MINESWEEPER, HEXMINE -> MinesweeperStage.parse(stage) != null
        HEXLASER -> StageType.parse(stage) != null
        else -> false
    }

    fun isValid(gameName: String, stage: String): Boolean =
        isValidGame(gameName) && isValidStage(gameName, stage)
}

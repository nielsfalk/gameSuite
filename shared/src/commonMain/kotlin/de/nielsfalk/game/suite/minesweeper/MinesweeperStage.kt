package de.nielsfalk.game.suite.minesweeper

import de.nielsfalk.game.suite.common.GameStage
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A Minesweeper difficulty. The [name] is the value used in deep links and score routes.
 *
 * Stages are fixed presets: a level of a stage is identified by its level number, not by board
 * parameters, so every stage can be numbered and reproduced from its level number alone.
 */
@Serializable
sealed interface MinesweeperStage : GameStage {
    val width: Int
    val height: Int
    val mines: Int

    val cells: Int get() = width * height

    @Serializable
    @SerialName("easy")
    data object Easy : MinesweeperStage {
        override val width: Int = 9
        override val height: Int = 9
        override val mines: Int = 10
        override val name: String = "easy"
    }

    @Serializable
    @SerialName("beginner")
    data object Beginner : MinesweeperStage {
        override val width: Int = 16
        override val height: Int = 16
        override val mines: Int = 40
        override val name: String = "beginner"
    }

    @Serializable
    @SerialName("expert")
    data object Expert : MinesweeperStage {
        override val width: Int = 16
        override val height: Int = 30
        override val mines: Int = 99
        override val name: String = "expert"
    }

    companion object {
        val presets: List<MinesweeperStage> = listOf(Easy, Beginner, Expert)

        fun parse(encoded: String): MinesweeperStage? = presets.firstOrNull { it.name == encoded }
    }
}

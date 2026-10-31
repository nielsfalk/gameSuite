package de.nielsfalk.game.suite.minesweeper

import de.nielsfalk.game.suite.common.GameStage
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A Minesweeper difficulty. The [name] is the value used in deep links.
 *
 * Custom stages encode their parameters in the name, e.g. `custom-12x10-20`.
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

    @Serializable
    @SerialName("custom")
    data class Custom(
        override val width: Int,
        override val height: Int,
        override val mines: Int
    ) : MinesweeperStage {
        init {
            require(width in MIN_SIZE..MAX_SIZE) {
                "width must be between $MIN_SIZE and $MAX_SIZE, was $width"
            }
            require(height in MIN_SIZE..MAX_SIZE) {
                "height must be between $MIN_SIZE and $MAX_SIZE, was $height"
            }
            require(mines >= 1) { "mines must be at least 1, was $mines" }
            require(mines <= cells - SAFE_FIRST_CLICK_CELLS) {
                "mines must leave room for a safe first click (at most ${cells - SAFE_FIRST_CLICK_CELLS}), was $mines"
            }
        }

        override val name: String get() = "custom-${width}x${height}-$mines"

        companion object {
            private val pattern = Regex("^custom-(\\d+)x(\\d+)-(\\d+)$")

            fun parse(encoded: String): Custom? {
                val match = pattern.matchEntire(encoded) ?: return null
                val (width, height, mines) = match.destructured
                return runCatching {
                    Custom(
                        width = width.toInt(),
                        height = height.toInt(),
                        mines = mines.toInt()
                    )
                }.getOrNull()
            }
        }
    }

    companion object {
        const val MIN_SIZE: Int = 5
        const val MAX_SIZE: Int = 30
        const val SAFE_FIRST_CLICK_CELLS: Int = 9

        val presets: List<MinesweeperStage> = listOf(Easy, Beginner, Expert)

        fun parse(encoded: String): MinesweeperStage? = when (encoded) {
            Easy.name -> Easy
            Beginner.name -> Beginner
            Expert.name -> Expert
            else -> Custom.parse(encoded)
        }
    }
}

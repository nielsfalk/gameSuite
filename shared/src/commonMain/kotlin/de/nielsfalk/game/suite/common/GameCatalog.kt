package de.nielsfalk.game.suite.common

import de.nielsfalk.game.suite.hexlaser.StageType
import de.nielsfalk.game.suite.hexlaser.next
import de.nielsfalk.game.suite.minesweeper.MinesweeperStage

/**
 * One game of the suite: its wire name, its ordered stages and its place in the navigation.
 *
 * The PWA shell, the server validation and the tests all work against this interface, so a new game
 * only has to be registered in [GameCatalog].
 */
interface SuiteGame {
    /** Name used in deep links, score routes and local storage. */
    val name: String

    /** Name shown in navigation and dialogs. */
    val displayName: String

    /** Short text shown on the game logo tile. */
    val tagline: String

    /** Stages of this game, from easiest to hardest. */
    val stages: List<String>

    /** The stage a game starts with. */
    val defaultStage: String

    /** The next harder stage, or `null` on the last stage. */
    fun nextStage(stage: String): String?

    /** Maps a stage from a deep link to its canonical name, or `null` when unknown. */
    fun normalizeStage(stage: String): String? = stages.firstOrNull { it == stage }

    /** Whether [stage] is the last stage of this game, where no stage-up happens. */
    fun isLastStage(stage: String): Boolean = nextStage(stage) == null
}

/** Minesweeper and HexMine share the same stage presets. */
private object MinesweeperStages {
    val names: List<String> = MinesweeperStage.presets.map { it.name }

    fun next(stage: String): String? {
        val index = names.indexOf(stage)
        return names.getOrNull(index + 1)
    }
}

data object MinesweeperGameInfo : SuiteGame {
    override val name: String = GameCatalog.MINESWEEPER
    override val displayName: String = "Minesweeper"
    override val tagline: String = "Clear the field"
    override val stages: List<String> = MinesweeperStages.names
    override val defaultStage: String = MinesweeperStage.Easy.name
    override fun nextStage(stage: String): String? = MinesweeperStages.next(stage)
}

data object HexMineGameInfo : SuiteGame {
    override val name: String = GameCatalog.HEXMINE
    override val displayName: String = "HexMine"
    override val tagline: String = "Mines on a hive"
    override val stages: List<String> = MinesweeperStages.names
    override val defaultStage: String = MinesweeperStage.Easy.name
    override fun nextStage(stage: String): String? = MinesweeperStages.next(stage)
}

data object HexlaserGameInfo : SuiteGame {
    override val name: String = GameCatalog.HEXLASER
    override val displayName: String = "Hexlaser"
    override val tagline: String = "Rotate the light"
    override val stages: List<String> = StageType.entries.map { it.wireName }
    override val defaultStage: String = StageType.entries.first().wireName
    override fun nextStage(stage: String): String? {
        val index = StageType.entries.indexOf(StageType.parse(stage) ?: return null)
        return StageType.entries.getOrNull(index + 1)?.wireName
    }
    override fun normalizeStage(stage: String): String? = StageType.parse(stage)?.wireName
}

/**
 * Source of truth for games and stages the platform accepts.
 *
 * The server uses this to reject score requests for unknown games or stages.
 */
object GameCatalog {
    const val MINESWEEPER: String = "minesweeper"
    const val HEXMINE: String = "hexmine"
    const val HEXLASER: String = "hexlaser"

    /** All games in navigation order. */
    val games: List<SuiteGame> = listOf(MinesweeperGameInfo, HexMineGameInfo, HexlaserGameInfo)

    val gameNames: Set<String> = games.map { it.name }.toSet()

    fun game(name: String): SuiteGame? = games.firstOrNull { it.name == name }

    fun isValidGame(gameName: String): Boolean = game(gameName) != null

    fun isValidStage(gameName: String, stage: String): Boolean =
        game(gameName)?.normalizeStage(stage) != null

    fun isValid(gameName: String, stage: String): Boolean =
        isValidGame(gameName) && isValidStage(gameName, stage)

    fun isValidLevel(level: String): Boolean =
        level.toIntOrNull()?.let { it >= LevelSeed.FIRST_LEVEL } ?: false

    /** The deterministic seed of one numbered level. */
    fun seed(gameName: String, stage: String, level: Int): Long = LevelSeed.of(gameName, stage, level)
}

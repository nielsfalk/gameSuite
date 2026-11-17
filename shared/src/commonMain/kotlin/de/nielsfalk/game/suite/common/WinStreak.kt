package de.nielsfalk.game.suite.common

/**
 * Counts consecutive wins of one game session and unlocks the stage-up dialog once per stage.
 *
 * The counter starts at zero and is reset by a loss or a restart of the current level. Navigating to
 * another level of the same stage keeps the streak, a stage change creates a new session.
 */
class WinStreak(val winsRequired: Int = DEFAULT_WINS_REQUIRED) {
    companion object {
        const val DEFAULT_WINS_REQUIRED: Int = 3
    }

    var consecutiveWins: Int = 0
        private set

    /** Whether the stage-up dialog was already unlocked in this stage. */
    var stageUpUnlocked: Boolean = false
        private set

    /** Records a win and returns `true` when the stage-up dialog should be shown now. */
    fun recordWin(): Boolean {
        if (stageUpUnlocked) return false
        consecutiveWins++
        if (consecutiveWins < winsRequired) return false
        stageUpUnlocked = true
        return true
    }

    /** Resets the streak after a loss. No score is posted — only wins are recorded. */
    fun recordLoss() {
        consecutiveWins = 0
    }

    /** Resets the streak when the current level is restarted. */
    fun restart() {
        consecutiveWins = 0
    }

    /** Wins still needed before the stage-up dialog unlocks. */
    val winsMissing: Int get() = (winsRequired - consecutiveWins).coerceAtLeast(0)
}

package de.nielsfalk.game.suite.server

import de.nielsfalk.game.suite.common.Score
import io.kotest.core.spec.style.FreeSpec
import java.util.UUID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere

/**
 * Exercises the real database configured through `DATABASE_URL` (credentials embedded in the URL, as Neon
 * provides) or `DB_URL` + `DB_USER` + `DB_PASSWORD`. Without such configuration the tests are ignored.
 * Run it locally with `./gradlew :server:databaseManualTest`, which loads the git-ignored `.env.local`.
 */
class DatabaseManualTest : FreeSpec({
    val config = manualJdbcConfig()
    val enabled = config != null

    "Database manual tests" - {
        "writes, reads and cleans up a score" - {
            val marker = "manual-test-${UUID.randomUUID()}"

            "persists the score".config(enabled = enabled) {
                DatabaseFactory.init(config!!)
                ScoreRepository().saveScore(
                    Score(marker, "minesweeper", "easy", "manual", time = 1234L, points = 42L)
                )
            }

            "reads the score and its highscore summary back".config(enabled = enabled) {
                val scores = ScoreRepository().getScores(marker, "minesweeper", "easy", "manual")
                assert(scores.map { it.points } == listOf(42L))

                val summary = ScoreRepository().getHighscores("minesweeper", "easy", "manual")
                assert(summary.topScores.any { it.username == marker && it.points == 42L })
                assert(summary.highestScore == 42L)
            }

            "deletes the score again".config(enabled = enabled) {
                val deleted = DatabaseFactory.dbQuery {
                    ScoresTable.deleteWhere { ScoresTable.username eq marker }
                }
                assert(deleted == 1)
                assert(ScoreRepository().getScores(marker, "minesweeper", "easy", "manual").isEmpty())
            }
        }
    }
})

private fun manualJdbcConfig(): JdbcConfig? {
    (System.getProperty("databaseUrl") ?: System.getenv("DATABASE_URL"))?.let {
        return postgresUrlToJdbc(it)
    }
    val url = System.getProperty("dbUrl") ?: System.getenv("DB_URL") ?: return null
    return JdbcConfig(
        url = url,
        user = System.getProperty("dbUser") ?: System.getenv("DB_USER") ?: "sa",
        password = System.getProperty("dbPassword") ?: System.getenv("DB_PASSWORD") ?: ""
    )
}

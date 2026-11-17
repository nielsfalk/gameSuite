package de.nielsfalk.game.suite.server

import de.nielsfalk.game.suite.common.HighscoreSummary
import de.nielsfalk.game.suite.common.Score
import de.nielsfalk.game.suite.common.ScoreSummary
import de.nielsfalk.game.suite.common.UserSummary
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object ScoresTable : Table("scores") {
    val id = integer("id").autoIncrement()
    val username = varchar("username", 100)
    val gameName = varchar("game_name", 100)
    val stage = varchar("stage", 100)
    val levelId = varchar("level_id", 100)
    val time = long("time")
    val points = long("points")
    val won = bool("won").default(true)

    override val primaryKey = PrimaryKey(id)
}

data class JdbcConfig(val url: String, val user: String, val password: String)

/**
 * Converts a libpq-style URL (`postgresql://user:password@host/db?sslmode=require`, as provided by Neon in
 * `DATABASE_URL`) into a JDBC URL plus separate credentials. Only `sslmode` is kept as a query parameter.
 */
fun postgresUrlToJdbc(databaseUrl: String): JdbcConfig {
    val uri = java.net.URI(databaseUrl)
    require(uri.scheme == "postgresql" || uri.scheme == "postgres") { "Unsupported scheme: ${uri.scheme}" }
    val decode = { s: String -> java.net.URLDecoder.decode(s, Charsets.UTF_8) }
    val (user, password) = (uri.rawUserInfo ?: "").split(":", limit = 2).let {
        decode(it[0]) to decode(it.getOrElse(1) { "" })
    }
    val sslmode = uri.rawQuery?.split("&")
        ?.map { it.split("=", limit = 2) }
        ?.firstOrNull { it[0] == "sslmode" }
        ?.getOrNull(1)
    val port = if (uri.port == -1) "" else ":${uri.port}"
    val query = sslmode?.let { "?sslmode=$it" } ?: ""
    return JdbcConfig("jdbc:postgresql://${uri.host}$port${uri.rawPath}$query", user, password)
}

private fun defaultJdbcConfig(): JdbcConfig {
    System.getenv("DB_URL")?.let {
        return JdbcConfig(it, System.getenv("DB_USER") ?: "sa", System.getenv("DB_PASSWORD") ?: "")
    }
    System.getenv("DATABASE_URL")?.let { return postgresUrlToJdbc(it) }
    return JdbcConfig("jdbc:h2:mem:gamesuite;DB_CLOSE_DELAY=-1", "sa", "")
}

object DatabaseFactory {
    fun init(
        config: JdbcConfig = defaultJdbcConfig(),
        url: String = config.url,
        driver: String = if (url.startsWith("jdbc:postgresql:")) "org.postgresql.Driver" else "org.h2.Driver",
        user: String = config.user,
        password: String = config.password
    ) {
        val db = Database.connect(
            url = url,
            driver = driver,
            user = user,
            password = password
        )
        transaction(db) {
            SchemaUtils.createMissingTablesAndColumns(ScoresTable)
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}

private fun ResultRow.toScore(): Score = Score(
    username = this[ScoresTable.username],
    gameName = this[ScoresTable.gameName],
    stage = this[ScoresTable.stage],
    levelId = this[ScoresTable.levelId],
    time = this[ScoresTable.time],
    points = this[ScoresTable.points],
    won = this[ScoresTable.won]
)

class ScoreRepository {
    suspend fun saveScore(score: Score): Score = DatabaseFactory.dbQuery {
        ScoresTable.insert {
            it[username] = score.username
            it[gameName] = score.gameName
            it[stage] = score.stage
            it[levelId] = score.levelId
            it[time] = score.time
            it[points] = score.points
            it[won] = score.won
        }
        score
    }

    suspend fun getScores(
        username: String,
        gameName: String,
        stage: String,
        level: String
    ): List<Score> = DatabaseFactory.dbQuery {
        ScoresTable.selectAll()
            .where {
                (ScoresTable.username eq username) and
                    (ScoresTable.gameName eq gameName) and
                    (ScoresTable.stage eq stage) and
                    (ScoresTable.levelId eq level)
            }
            .map { it.toScore() }
            .sortedWith(ScoreSummary.highscoreOrder)
    }

    /**
     * The level highscore: the top [limit] won attempts ranked by points and time plus the number of
     * attempts (won or lost) on that level.
     */
    suspend fun getHighscores(
        gameName: String,
        stage: String,
        level: String,
        limit: Int = 10
    ): HighscoreSummary = DatabaseFactory.dbQuery {
        val all = ScoresTable.selectAll()
            .where {
                (ScoresTable.gameName eq gameName) and
                    (ScoresTable.stage eq stage) and
                    (ScoresTable.levelId eq level)
            }
            .map { it.toScore() }
        val won = all.filter { it.won }.sortedWith(ScoreSummary.highscoreOrder)

        HighscoreSummary(
            gameName = gameName,
            stage = stage,
            levelId = level,
            topScores = won.take(limit),
            totalPlays = all.size.toLong(),
            highestScore = won.firstOrNull()?.points
        )
    }

    /** All scores of one user, used to build the summary endpoint. */
    suspend fun getScoresOfUser(username: String): List<Score> = DatabaseFactory.dbQuery {
        ScoresTable.selectAll()
            .where { ScoresTable.username eq username }
            .map { it.toScore() }
    }

    /** Per game and stage aggregates of one user. */
    suspend fun getSummary(username: String): UserSummary =
        ScoreSummary.summarizeUser(username, getScoresOfUser(username))
}

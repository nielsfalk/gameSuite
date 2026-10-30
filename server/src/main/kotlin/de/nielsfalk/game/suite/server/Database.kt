package de.nielsfalk.game.suite.server

import de.nielsfalk.game.suite.common.HighscoreSummary
import de.nielsfalk.game.suite.common.Score
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.max
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

    override val primaryKey = PrimaryKey(id)
}

object DatabaseFactory {
    fun init(
        url: String = System.getenv("DB_URL") ?: "jdbc:h2:mem:gamesuite;DB_CLOSE_DELAY=-1",
        driver: String = if (url.startsWith("jdbc:postgresql:")) "org.postgresql.Driver" else "org.h2.Driver",
        user: String = System.getenv("DB_USER") ?: "sa",
        password: String = System.getenv("DB_PASSWORD") ?: ""
    ) {
        val db = Database.connect(
            url = url,
            driver = driver,
            user = user,
            password = password
        )
        transaction(db) {
            SchemaUtils.create(ScoresTable)
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}

class ScoreRepository {
    suspend fun saveScore(score: Score): Score = DatabaseFactory.dbQuery {
        ScoresTable.insert {
            it[username] = score.username
            it[gameName] = score.gameName
            it[stage] = score.stage
            it[levelId] = score.levelId
            it[time] = score.time
            it[points] = score.points
        }
        score
    }

    suspend fun getScores(
        username: String,
        gameName: String,
        stage: String,
        levelId: String
    ): List<Score> = DatabaseFactory.dbQuery {
        ScoresTable.selectAll()
            .where {
                (ScoresTable.username eq username) and
                    (ScoresTable.gameName eq gameName) and
                    (ScoresTable.stage eq stage) and
                    (ScoresTable.levelId eq levelId)
            }
            .orderBy(ScoresTable.points to SortOrder.DESC, ScoresTable.time to SortOrder.ASC)
            .map {
                Score(
                    username = it[ScoresTable.username],
                    gameName = it[ScoresTable.gameName],
                    stage = it[ScoresTable.stage],
                    levelId = it[ScoresTable.levelId],
                    time = it[ScoresTable.time],
                    points = it[ScoresTable.points]
                )
            }
    }

    suspend fun getHighscores(
        gameName: String,
        stage: String,
        levelId: String,
        limit: Int = 10
    ): HighscoreSummary = DatabaseFactory.dbQuery {
        val query = ScoresTable.selectAll()
            .where {
                (ScoresTable.gameName eq gameName) and
                    (ScoresTable.stage eq stage) and
                    (ScoresTable.levelId eq levelId)
            }
            .orderBy(ScoresTable.points to SortOrder.DESC, ScoresTable.time to SortOrder.ASC)

        val totalPlays = query.count()
        val topScores = query.limit(limit).map {
            Score(
                username = it[ScoresTable.username],
                gameName = it[ScoresTable.gameName],
                stage = it[ScoresTable.stage],
                levelId = it[ScoresTable.levelId],
                time = it[ScoresTable.time],
                points = it[ScoresTable.points]
            )
        }
        val maxPoints = topScores.firstOrNull()?.points

        HighscoreSummary(
            gameName = gameName,
            stage = stage,
            levelId = levelId,
            topScores = topScores,
            totalPlays = totalPlays,
            highestScore = maxPoints
        )
    }
}

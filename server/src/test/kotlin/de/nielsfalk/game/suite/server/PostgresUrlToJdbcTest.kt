package de.nielsfalk.game.suite.server

import io.kotest.core.spec.style.FreeSpec

class PostgresUrlToJdbcTest : FreeSpec({
    "postgresUrlToJdbc" - {
        data class Case(val input: String, val expected: JdbcConfig)

        listOf(
            Case(
                "postgresql://alice:s3cret@ep-x-pooler.eu-central-1.aws.neon.tech/neondb?channel_binding=require&sslmode=require",
                JdbcConfig("jdbc:postgresql://ep-x-pooler.eu-central-1.aws.neon.tech/neondb?sslmode=require", "alice", "s3cret")
            ),
            Case(
                "postgres://bob:p%40ss@localhost:5433/games",
                JdbcConfig("jdbc:postgresql://localhost:5433/games", "bob", "p@ss")
            )
        ).forEach { (input, expected) ->
            "converts $input" {
                assert(postgresUrlToJdbc(input) == expected)
            }
        }
    }
})

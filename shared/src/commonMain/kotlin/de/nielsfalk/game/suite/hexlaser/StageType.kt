package de.nielsfalk.game.suite.hexlaser

enum class StageType(
    vararg properties: StageProperties,
    val infiniteX: Boolean = true,
    val infiniteY: Boolean = false
) {
    ABSOLUTE_BEGINNER(
        StageProperties(x = 1, y = 3, sourceCount = 1, rotateObvious = true, maxPrismaCount = 0),
        StageProperties(x = 2, y = 2, sourceCount = 1, rotateObvious = true, maxPrismaCount = 0),
        infiniteX = false
    ),
    EASY(
        StageProperties(x = 2, sourceCount = 1, rotateObvious = true, maxPrismaCount = 0),
        StageProperties(x = 2, y = 3, sourceCount = 2, rotateObvious = true, maxPrismaCount = 0),
        infiniteX = false
    ),
    INTERMEDIATE(
        StageProperties(x = 3, sourceCount = 2, maxPrismaCount = 1),
        StageProperties(x = 3, sourceCount = 3, maxPrismaCount = 1),
        StageProperties(x = 3, sourceCount = 4, maxPrismaCount = 2),
        infiniteX = false
    ),
    INTERMEDIATE_PLUS(
        StageProperties(x = 3, y = 3, sourceCount = 2, maxPrismaCount = 0),
    ),
    INTERMEDIATE_PLUS2(
        StageProperties(x = 3, sourceCount = 2, maxPrismaCount = 1),
        StageProperties(x = 3, sourceCount = 3, maxPrismaCount = 1),
        StageProperties(x = 3, sourceCount = 4, maxPrismaCount = 2),
    ),
    HARD(
        StageProperties(x = 4, sourceCount = 1, maxPrismaCount = 3),
        StageProperties(x = 4, sourceCount = 2, maxPrismaCount = 3),
        StageProperties(x = 4, sourceCount = 3, maxPrismaCount = 3),
        StageProperties(x = 4, sourceCount = 9, maxPrismaCount = 3),
        StageProperties(x = 4, sourceCount = 10, maxPrismaCount = 3),
    ),
    HARDER(
        StageProperties(x = 5, sourceCount = 1),
        StageProperties(x = 5, sourceCount = 2),
        StageProperties(x = 5, sourceCount = 3),
        StageProperties(x = 5, sourceCount = 19),
        StageProperties(x = 5, sourceCount = 20),
        StageProperties(x = 6, sourceCount = 30),
        StageProperties(x = 6, sourceCount = 35)
    ),
    INSANE(
        StageProperties(x = 4, y = 4, sourceCount = 2, maxPrismaCount = 0),
        StageProperties(x = 4, y = 4, sourceCount = 3, maxPrismaCount = 0),
        StageProperties(x = 4, y = 4, sourceCount = 4, maxPrismaCount = 0),
        infiniteX = true,
        infiniteY = true
    ),
    NIGHTMARE(
        StageProperties(x = 4, sourceCount = 1),
        StageProperties(x = 4, sourceCount = 2),
        StageProperties(x = 4, sourceCount = 3),
        StageProperties(x = 4, sourceCount = 9),
        StageProperties(x = 4, sourceCount = 10),
        infiniteX = true,
        infiniteY = true
    ),
    NIGHTMARE_PLUS(
        StageProperties(x = 6, sourceCount = 1),
        StageProperties(x = 6, sourceCount = 2),
        StageProperties(x = 6, sourceCount = 3),
        StageProperties(x = 6, sourceCount = 4),
        StageProperties(x = 6, sourceCount = 29),
        StageProperties(x = 6, sourceCount = 30),
        infiniteX = true,
        infiniteY = true
    ),
    NIGHTMARE_PLUS2(
        StageProperties(x = 8, sourceCount = 1),
        StageProperties(x = 8, sourceCount = 2),
        StageProperties(x = 8, sourceCount = 3),
        StageProperties(x = 8, sourceCount = 4),
        StageProperties(x = 8, sourceCount = 4),
        StageProperties(x = 8, sourceCount = 5),
        StageProperties(x = 8, sourceCount = 6),
        StageProperties(x = 8, sourceCount = 30),
        StageProperties(x = 8, sourceCount = 40),
        infiniteX = true,
        infiniteY = true
    );

    val stageProperties = properties.toList()

    val wireName: String get() = name.lowercase()

    companion object {
        fun parse(encoded: String): StageType? =
            entries.firstOrNull { it.wireName == encoded.lowercase() }
    }
}

fun StageType.next(): StageType =
    StageType.entries[(ordinal + 1) % StageType.entries.size]

data class StageProperties(
    val x: Int = 3,
    val y: Int = x * 2,
    val sourceCount: Int = 3,
    val rotateObvious: Boolean = false,
    val maxPrismaCount: Int = 17
)

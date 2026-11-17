package de.nielsfalk.game.suite.common

/**
 * Deterministic random seeds for numbered levels.
 *
 * Every stage has levels numbered `1..∞`. A level is identified by game name, stage and level number,
 * and that triple is hashed into the seed of the random number generator. The same level therefore
 * always reproduces the same board, on every device and in every session.
 */
object LevelSeed {
    /** The lowest level number of every stage. */
    const val FIRST_LEVEL: Int = 1

    /**
     * Hashes [gameName], [stage] and [level] into a stable seed.
     *
     * The implementation is a plain FNV-1a hash followed by a final avalanche mix, so it does not depend
     * on platform `hashCode` implementations and always returns a non-zero seed.
     */
    fun of(gameName: String, stage: String, level: Int): Long {
        require(level >= FIRST_LEVEL) { "level must be >= $FIRST_LEVEL, was $level" }

        val input = "$gameName\u0000$stage\u0000$level"
        var hash = FNV_OFFSET_BASIS
        input.forEach { char ->
            hash = hash xor char.code.toLong()
            hash *= FNV_PRIME
        }
        hash = hash xor (hash ushr 33)
        hash *= -0xae502812aa7333L
        hash = hash xor (hash ushr 33)
        return if (hash == 0L) -1L else hash
    }

    /** Parses a level number from a deep link, falling back to [FIRST_LEVEL] for legacy or invalid ids. */
    fun parse(value: String?): Int = value?.toIntOrNull()?.takeIf { it >= FIRST_LEVEL } ?: FIRST_LEVEL

    /** Renders a level number for deep links and score submissions. */
    fun format(level: Int): String = level.coerceAtLeast(FIRST_LEVEL).toString()

    private const val FNV_OFFSET_BASIS: Long = -0x340d631b7bdddcdbL
    private const val FNV_PRIME: Long = 0x100000001b3L
}

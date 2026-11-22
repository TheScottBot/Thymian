package fyi.punkdev.thymian.domain

import kotlin.random.Random

/**
 * Shared mutations for the seeded mutation tests that stand in for fuzzing
 * (CWD20): random byte damage, and swapping a number for a value at or beyond
 * a range boundary, since byte damage alone almost never forms a well formed
 * out of range number (found by planting a missing range check, TESTING.md).
 */
object SeededMutations {
    const val SEED = 20_261_122

    private val jsonFragments = listOf("{", "}", "[", "]", ":", ",", "\"", "null", "true", "1e999", "-0", "\\u0000", "9".repeat(40))

    private val boundaryNumbers =
        listOf(
            "70",
            "70.1",
            "71",
            "-100",
            "-100.1",
            "-101",
            "90",
            "90.1",
            "-90.1",
            "180",
            "180.1",
            "-180.1",
            "999",
            "-999",
            "1e308",
            "-1e308",
            "0",
            "-0",
            "1",
            "2",
            "-1",
            "99",
            "100",
            "2.5",
        )

    private val numberPattern = Regex("""-?\d+(\.\d+)?([eE][+-]?\d+)?""")

    fun mutated(
        original: ByteArray,
        random: Random,
    ): ByteArray = if (random.nextBoolean()) damageBytes(original, random) else replaceOneNumber(original, random)

    private fun damageBytes(
        original: ByteArray,
        random: Random,
    ): ByteArray {
        val bytes = original.toMutableList()
        repeat(random.nextInt(1, 6)) {
            if (bytes.isEmpty()) return@repeat
            val position = random.nextInt(bytes.size)
            when (random.nextInt(5)) {
                0 -> bytes[position] = random.nextInt(256).toByte()
                1 -> bytes.removeAt(position)
                2 -> bytes.addAll(position, jsonFragments.random(random).encodeToByteArray().toList())
                3 -> while (bytes.size > position) bytes.removeAt(bytes.size - 1)
                else -> bytes.addAll(position, bytes.subList(position, minOf(bytes.size, position + random.nextInt(1, 40))).toList())
            }
        }
        return bytes.toByteArray()
    }

    private fun replaceOneNumber(
        original: ByteArray,
        random: Random,
    ): ByteArray {
        val text = original.decodeToString()
        val numbers = numberPattern.findAll(text).toList()
        if (numbers.isEmpty()) return original
        val chosen = numbers.random(random)
        return text.replaceRange(chosen.range, boundaryNumbers.random(random)).encodeToByteArray()
    }
}

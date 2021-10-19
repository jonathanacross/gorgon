package gorgon.pextract

import gorgon.gobase.GoBoard
import java.lang.Math.abs

// Handles up to 7x7 patterns; larger would require more bits
data class Pattern(
    val size: Int,
    val blackBits: Long,
    val whiteBits: Long
) : Comparable<Pattern> {
    init { require(size == 3 || size == 5 || size == 7) }

    fun countMinSketchHash(filterNum: Int): Long {
        // These big constants are picked as NextPrime(RandInt(2^63))
        // Currently we support up to 7 min filter hashes.
        val primes = listOf(
            0x7f8efc50ea332ff5,
            0x2f38a776dbfbe1b3,
            0x3e12b67396ef0df9,
            0x61bdec0bc5d9c7bf,
            0x1694fd515caebf49,
            0x2931e7ea6e559c7,
            0x1ee2487e8477a303
        )
        var result: Long = 1
        result = result * primes[filterNum] + size
        result = result * primes[filterNum] + blackBits
        result = result * primes[filterNum] + whiteBits

        return kotlin.math.abs(result)
    }

    override fun compareTo(other: Pattern): Int {
       return compareValuesBy(this, other, {it.size}, {it.blackBits}, {it.whiteBits})
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append(size)
        sb.append("|")
        for (idx in size * size - 1 downTo 0) {
            val black = (blackBits shr idx) and 1
            val white = (whiteBits shr idx) and 1
            val squareType = 2 * black + white
            val charRep = when (squareType) {
                0L -> '.'
                1L -> 'O'
                2L -> 'X'
                3L -> '#'
                else -> '?'
            }
            sb.append(charRep)
            if (idx % size == 0) {
                sb.append("|")
            }
        }
        return sb.toString()
    }

    companion object {
        // boardUp = size of index increase when moving up the board
        // (roughly equal to the size of the board)
        fun getBoardOffsets(size: Int, boardUp: Int): IntArray {
            val patternRadius = (size - 1) / 2
            var offsets = IntArray(size * size)
            var idx = 0
            for (row in -patternRadius..patternRadius) {
                for (col in -patternRadius..patternRadius) {
                    offsets[idx] = row * boardUp + col
                    idx++
                }
            }
            return offsets
        }

        // returns 8 patterns of offsets for indices to extract a pattern from a board,
        // The 8 corresponds to the 8 symmetries of rotating and reflecting
        // i = identity
        // r = clockwise rotation
        // s = vertical reflection
        fun getPatternOffsets(size: Int): List<IntArray> {
            var id = IntArray(size * size)
            var s = IntArray(size * size)
            var rrr = IntArray(size * size)
            var srrr = IntArray(size * size)
            var rr = IntArray(size * size)
            var sr = IntArray(size * size)
            var srr = IntArray(size * size)
            var r = IntArray(size * size)
            for (row in 0 until size) {
                for (col in 0 until size) {
                    val patternIdx = size * row + col
                    val nrow = size - row - 1
                    val ncol = size - col - 1
                    id[row * size + col] = patternIdx
                    r[col * size + nrow] = patternIdx
                    rr[nrow * size + ncol] = patternIdx
                    rrr[ncol * size + row] = patternIdx
                    s[nrow * size + col] = patternIdx
                    sr[ncol * size + nrow] = patternIdx
                    srr[row * size + ncol] = patternIdx
                    srrr[col * size + row] = patternIdx
                }
            }
            return listOf(id, s, rrr, srrr, sr, rr, srr, r)
        }
    }
}
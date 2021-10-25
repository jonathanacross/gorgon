package gorgon.pextract

import gorgon.gobase.GoBoard
import gorgon.gobase.Location
import gorgon.gobase.Player
import gorgon.gobase.SquareType

private data class PatternBits(var blackBits: Long, var whiteBits: Long) {

    fun adjustBits(squareType: Int, idx: Int, player: Player) {
        if (player == Player.Black) {
            adjustBitsSame(squareType, idx)
        } else {
            // reverse colors for white player
            adjustBitsOpposite(squareType, idx)
        }
    }

    private fun adjustBitsOpposite(squareType: Int, idx: Int) {
        if (squareType == SquareType.White || squareType == SquareType.OffBoard) {
            blackBits = blackBits or (1L shl idx)
        }
        if (squareType == SquareType.Black || squareType == SquareType.OffBoard) {
            whiteBits = whiteBits or (1L shl idx)
        }
    }

    private fun adjustBitsSame(squareType: Int, idx: Int) {
        if (squareType == SquareType.Black || squareType == SquareType.OffBoard) {
            blackBits = blackBits or (1L shl idx)
        }
        if (squareType == SquareType.White || squareType == SquareType.OffBoard) {
            whiteBits = whiteBits or (1L shl idx)
        }
    }
}

class PatternExtractor(val patternSize: Int) {

    private val patternOffsets = Pattern.getPatternOffsets(patternSize)

    fun getSmallestPattern(patterns: List<Pattern>): Pattern {
        // find the smallest pattern. Equivalent code: though slower:
        // val smallestPattern = patterns.minOrNull()!!
        var smallestPattern = patterns[0]
        var first = true
        for (pattern in patterns) {
            if (first) {
                smallestPattern = pattern
                first = false
                continue
            }
            if (pattern.blackBits < smallestPattern.blackBits ||
                (pattern.blackBits == smallestPattern.blackBits &&
                        pattern.whiteBits <= smallestPattern.whiteBits)
            ) {
                smallestPattern = pattern
            }
        }
        return smallestPattern
    }

    fun getPatternAt(board: GoBoard, location: Int, player: Player): Pattern {
        val numSymmetries = patternOffsets.size

        val patternBits = Array(numSymmetries) { PatternBits(0L, 0L) }

        val radius = patternSize / 2
        val (r, c) = Location.idxToRowCol(location)
        var idx = 0
        for (dx in -radius..radius) {
            for (dy in -radius..radius) {
                val rr = r + dy
                val cc = c + dx
                val value = if (rr < 1 || rr > board.size || cc < 1 || cc > board.size) {
                    SquareType.OffBoard
                } else {
                    board.data[Location.rowColToIdx(rr, cc)]
                }

                for (i in 0 until numSymmetries) {
                    patternBits[i].adjustBits(value, patternOffsets[i][idx], player)
                }

                idx++
            }
        }

        val patterns = patternBits.map { x -> Pattern(patternSize, x.blackBits, x.whiteBits) }
        val smallestPattern = getSmallestPattern(patterns)

        return smallestPattern
    }
}
package gorgon.pextract

import gorgon.gobase.GoBoard
import gorgon.gobase.Location
import gorgon.gobase.SquareType

private data class PatternBits(var blackBits: Long, var whiteBits: Long) {
    fun adjustBits(squareType: Int, idx: Int) {
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

    fun getPatternAt(board: GoBoard, location: Int): Pattern {
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
                    patternBits[i].adjustBits(value, patternOffsets[i][idx])
                }

                idx++
            }
        }

        val patterns = patternBits.map { x -> Pattern(patternSize, x.blackBits, x.whiteBits) }
        val smallestPattern = patterns.minOrNull()!!

        return smallestPattern
    }
}
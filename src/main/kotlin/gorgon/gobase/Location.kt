package gorgon.gobase

// We refer to board locations as a 1-D array, on a fixed size board
// so that it's very easy/fast to convert from positions to indices.
//
// The indices for bottom two rows of the board, plus one edge of outside border
// are shown below (assuming max size playable board is 19x19).  Note that on
// the left and the right, the border indices are repeated.
//
// 2 | 41 | 42 43 44 45 46 47 48 49 50 51 52 53 54 55 56 57 58 59 60 | 61 |
// 1 | 21 | 22 23 24 25 26 27 28 29 30 31 32 33 34 35 36 37 38 39 40 | 41 |
//   +----+----------------------------------------------------------+----+
//   |  0 |  1  2  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19 20 | 21 |
//   +----+----------------------------------------------------------+----+
//           A  B  C  D  E  F  G  H  J  K  L  M  N  O  P  Q  R  S  T
//
// Then, referring to adjacent squares is easily done by an offset, e.g.,
// up(x) = x+20.
//
// We indicate "pass" as the special location -1.
object Location {
    const val minBoardSize = 1
    const val maxBoardSize = 19
    const val numLocs = (maxBoardSize + 1) * (maxBoardSize + 2) + 1
    const val pass = -1
    const val undefined = -99

    private val colNames = "ABCDEFGHJKLMNOPQRSTUVWXYZ".toCharArray().toList()

    // row, col are 1-based
    fun rowColToIdx(row: Int, col: Int): Int {
        return row * (maxBoardSize + 1) + col
    }

    fun idxToRowCol(idx: Int): Pair<Int, Int> {
        val row = (idx - 1) / (maxBoardSize + 1)
        val col = (idx - 1) % (maxBoardSize + 1)
        return Pair(row, col + 1)
    }

    fun stringToIdx(locString: String): Int {
        return if (locString.toLowerCase() == "pass")
            pass
        else {
            val colChar = locString[0].toUpperCase()
            val col = colNames.indexOf(colChar) + 1
            val row = locString.substring(1).toInt()
            val idx = rowColToIdx(row, col)
            idx
        }
    }

    fun idxToString(index: Int): String {
        return when (index) {
            undefined -> {
                "undefined"
            }
            pass -> {
                "pass"
            }
            else -> {
                val row = index / (maxBoardSize + 1)
                val col = index % (maxBoardSize + 1)
                val locString = colNames[col - 1] + row.toString()
                locString
            }
        }
    }

    fun up(idx: Int) = idx + maxBoardSize + 1
    fun down(idx: Int) = idx - maxBoardSize - 1
    fun left(idx: Int) = idx - 1
    fun right(idx: Int) = idx + 1
    fun northEast(idx: Int) = idx + maxBoardSize + 2
    fun southEast(idx: Int) = idx - maxBoardSize
    fun southWest(idx: Int) = idx - maxBoardSize - 2
    fun northWest(idx: Int) = idx + maxBoardSize

    const val up = maxBoardSize + 1
    const val down = -maxBoardSize - 1
    const val left = -1
    const val right = 1
    const val northEast = maxBoardSize + 2
    const val southEast = -maxBoardSize
    const val northWest = -maxBoardSize - 2
    const val southWest = maxBoardSize
}
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
    val maxBoardSize = 19
    val numLocs = (maxBoardSize + 1) * (maxBoardSize + 2) + 1
    val pass = -1
    val undefined = -99

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
            Location.pass
        else {
            val colChar = locString[0].toUpperCase()
            val col = colNames.indexOf(colChar) + 1
            val row = locString.substring(1).toInt()
            val idx = rowColToIdx(row, col)
            idx
        }
    }

    fun idxToString(index: Int): String {
        return if (index == Location.undefined) {
            "undefined"
        } else if (index == Location.pass) {
            "pass"
        } else {
            val row = index / (maxBoardSize + 1)
            val col = index % (maxBoardSize + 1)
            val locString = colNames[col - 1] + row.toString()
            locString
        }
    }

    fun up(idx: Int) = idx + maxBoardSize + 1
    fun down(idx: Int) = idx - maxBoardSize - 1
    fun left(idx: Int) = idx - 1
    fun right(idx: Int) = idx + 1
    fun NE(idx: Int) = idx + maxBoardSize + 2
    fun SE(idx: Int) = idx - maxBoardSize
    fun SW(idx: Int) = idx - maxBoardSize - 2
    fun NW(idx: Int) = idx + maxBoardSize

    val up = maxBoardSize + 1
    val down = -maxBoardSize - 1
    val left = -1
    val right = 1
    val NE = maxBoardSize + 2
    val SE = -maxBoardSize
    val SW = -maxBoardSize - 2
    val NW = maxBoardSize
}
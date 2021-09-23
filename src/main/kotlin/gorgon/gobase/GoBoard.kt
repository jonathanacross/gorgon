package gorgon.gobase

data class GoBoard(
    val size: Int,
    val data: IntArray,
    val ko: Int = noKo,
    val blackStonesTaken: Int = 0,
    val whiteStonesTaken: Int = 0,
) {
    fun set(value: Int, idx: Int): GoBoard {
        val newData = data.clone()
        newData[idx] = value
        return GoBoard(size, newData)
    }

    fun set(value: Int, squares: List<Int>): GoBoard {
        val newData = data.clone()
        for (square in squares) {
            newData[square] = value
        }
        return GoBoard(size, newData)
    }

    fun getIdx(idx: Int): Int = data[idx]

    fun play(player: Player, idx: Int): GoBoard {
        if (idx < 0) {
            // pass
            return GoBoard(size, data.clone())
        } else {
            val value = SquareType.playerToSquareType(player)
            // TODO: handle captures and ko
            val newData = data.clone()
            newData[idx] = value
            val koLocation = noKo

            return GoBoard(
                size,
                newData,
                koLocation,
                blackStonesTaken,
                whiteStonesTaken
            )
        }
    }

    override fun toString(): String {
        val allColNames = "ABCDEFGHJKLMNOPQRSTUVWXYZ"
        val sb = StringBuilder()

        val colNames = allColNames.substring(0, size).split("").joinToString(" ")
        // top border
        sb.append("\n")
        sb.append("  " + colNames + "\n")

        // main board
        for (r in size downTo 1) {
            val rowName = "%2d".format(r)
            sb.append(rowName + " ")
            for (c in 1..size) {
                sb.append(SquareType.printForm(data[Location.rowColToIdx(r, c)]) + " ")
            }
            sb.append(rowName + "\n")
        }

        // bottom border
        sb.append("  " + colNames)

        return sb.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GoBoard

        if (size != other.size) return false
        if (!data.contentEquals(other.data)) return false
        if (ko != other.ko) return false
        if (blackStonesTaken != other.blackStonesTaken) return false
        if (whiteStonesTaken != other.whiteStonesTaken) return false

        return true
    }

    // Returns the set of location indices that are on the board.
    fun boardSquares() = boardSquaresBySize[size]

    fun positionalHash(): Long {
        var h: Long = 0
        for (i in boardSquares()) {
            h = h xor ZobristHash.getHash(data[i], i)
        }
        h = h xor ZobristHash.getKoHash(ko)
        return h
    }

    override fun hashCode(): Int {
        var result = size
        result = 31 * result + data.contentHashCode()
        result = 31 * result + ko
        result = 31 * result + blackStonesTaken
        result = 31 * result + whiteStonesTaken
        return result
    }

    companion object {
        const val minSize = Location.minBoardSize
        const val maxSize = Location.maxBoardSize
        const val noKo = 0
        val boardSquaresBySize =
            Array(maxSize + 1) { i -> getBoardSquaresForSize(i) }

        private fun getEmptyBoardDataForSize(size: Int): IntArray {
            val data = IntArray(Location.numLocs) { SquareType.OffBoard }
            for (row in 1..size) {
                for (col in 1..size) {
                    data[Location.rowColToIdx(row, col)] = SquareType.Empty
                }
            }
            return data
        }

        private fun getBoardSquaresForSize(size: Int): IntArray {
            val data = getEmptyBoardDataForSize(size)
            return data.indices.filter { i -> data[i] != SquareType.OffBoard }.toIntArray()
        }

        fun emptyBoard(size: Int): GoBoard {
            require((size >= minSize) && (size <= maxSize))

            val data = getEmptyBoardDataForSize(size)
            return GoBoard(size, data, noKo)
        }
    }
}
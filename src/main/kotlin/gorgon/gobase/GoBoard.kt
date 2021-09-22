package gorgon.gobase

data class GoBoard(
    val size: Int,
    val data: IntArray,
    val ko: Int = noKo,
    val blackStonesTaken: Int = 0,
    val whiteStonesTaken: Int = 0,
    val boardSquares: IntArray
) {
    fun set(value: Int, idx: Int): GoBoard {
        val newData = data.clone()
        newData[idx] = value
        return GoBoard(size, newData, boardSquares = this.boardSquares)
    }

    fun set(value: Int, squares: List<Int>): GoBoard {
        val newData = data.clone()
        for (square in squares) {
            newData[square] = value
        }
        return GoBoard(size, newData, boardSquares = this.boardSquares)
    }

    fun getIdx(idx: Int): Int = data[idx]

    fun play(player: Player, idx: Int): GoBoard {
        if (idx < 0) {
            // pass
            return GoBoard(size, data.clone(), boardSquares = this.boardSquares)
        } else {
            val value = SquareType.playerToSquareType(player)
            // TODO: handle captures and ko
            val newdata = data.clone()
            newdata[idx] = value
            val koLocation = noKo

            return GoBoard(size, newdata, koLocation, blackStonesTaken, whiteStonesTaken, boardSquares = this.boardSquares)
        }
    }


    override fun toString(): String {
        val allcolnames = "ABCDEFGHJKLMNOPQRSTUVWXYZ"
        val sb = StringBuilder()

        val colnames = allcolnames.substring(0, size).split("").joinToString(" ")
        // top border
        sb.append("\n")
        sb.append("  " + colnames + "\n")

        // main board
        for (r in size downTo 1) {
            val rowname = "%2d".format(r)
            sb.append(rowname + " ")
            for (c in 1..size) {
                sb.append(SquareType.printForm(data[Location.rowColToIdx(r, c)]) + " ")
            }
            sb.append(rowname + "\n")
        }

        // bottom border
        sb.append("  " + colnames)

        return sb.toString()
    }

    companion object {
        val minSize = 1
        val maxSize = Location.maxBoardSize
        val noKo = -1

        fun emptyBoard(size: Int): GoBoard {
            require((size >= minSize) && (size <= maxSize))

            val data = IntArray(Location.numLocs) { SquareType.OffBoard }
            for (row in 1..size) {
                for (col in 1..size) {
                    data[Location.rowColToIdx(row, col)] = SquareType.Empty
                }
            }
            val boardSquares: IntArray =
                data.indices.filter { i -> data[i] != SquareType.OffBoard }.toIntArray()
            return GoBoard(size, data, noKo, boardSquares = boardSquares)
        }
    }
}
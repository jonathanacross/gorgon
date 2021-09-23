package gorgon.gobase

import java.util.*

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

    fun isLegalMove(squareType: Int, idx: Int): Boolean {
        // square must be empty (duh!)
        if (data[idx] != SquareType.Empty) {
            return false
        }

        if (idx == ko) {
            // Only retake a ko if it's a snapback.
            return getCapturedStoneList(squareType, idx).size > 1
        }

        // If there are liberties adjacent to current move, then for sure this
        // isn't suicide.
        if (data[Location.left(idx)] == SquareType.Empty ||
            data[Location.up(idx)] == SquareType.Empty ||
            data[Location.right(idx)] == SquareType.Empty ||
            data[Location.down(idx)] == SquareType.Empty
        ) {
            return true
        }

        // Suicide moves are legal only if we make a capture.
        if (isPotentialSuicide(squareType, idx)) {
            return getCapturedStoneList(squareType, idx).isNotEmpty()
        } else {
            return true
        }
    }

    private fun isPotentialSuicide(value: Int, idx: Int): Boolean {
        val supposePlay = this.set(value, idx)
        return supposePlay.isGroupSurrounded(idx)
    }

    fun legalMoves(player: Player): List<Int> {
        val squareType = SquareType.playerToSquareType(player)
        return boardSquares().filter { i -> isLegalMove(squareType, i) }.toList()
    }

    // TODO: rename to getCapturedStones?
    fun getCapturedStoneList(value: Int, idx: Int): List<Int> {
        val opp = SquareType.opposite(value)
        if ((data[Location.up(idx)] != opp) &&
            (data[Location.down(idx)] != opp) &&
            (data[Location.left(idx)] != opp) &&
            (data[Location.right(idx)] != opp)
        ) {
            return listOf()
        }

        require(data[idx] == SquareType.Empty)

        // temporarily place the stone
        data[idx] = value

        val captured = ArrayList<Int>()
        for (nbr in neighbors(idx)) {
            if (data[nbr] == opp) {
                if (isGroupSurrounded(nbr)) {
                    val group = floodfill(nbr, { x: Int -> x == data[nbr] })
                    captured.addAll(group)
                }
            }
        }

        // remove the stone
        data[idx] = SquareType.Empty
        return captured.toList()
    }

    private fun neighbors(idx: Int): List<Int> {
        val allnbrs =
            listOf(Location.left(idx), Location.right(idx), Location.up(idx), Location.down(idx))
        return allnbrs.filter { i -> data[i] != SquareType.OffBoard }
    }

    // Tromp-Taylor scoring. The Tromp-Taylor ruleset requires the game to be
    // played out until all dead stones are removed, then uses area (Chinese)
    // scoring.
    fun score(): Pair<Int, Int> {
        val whiteSquares = boardSquares().filter { i -> data[i] == SquareType.White }
        val blackSquares = boardSquares().filter { i -> data[i] == SquareType.Black }

        val reachableWhite = floodfillmulti(
            whiteSquares,
            { x: Int -> (x == SquareType.Empty || x == SquareType.White) }).toSet()
        val reachableBlack = floodfillmulti(
            blackSquares,
            { x: Int -> (x == SquareType.Empty || x == SquareType.Black) }).toSet()

        val whiteOnly = reachableWhite subtract reachableBlack
        val blackOnly = reachableBlack subtract reachableWhite

        return Pair(blackOnly.size, whiteOnly.size)
    }

    // flood fill an area that follows a given predicate, starting at the list
    // of squares startIdx.
    // TODO: more efficient algorithm, possibly http://will.thimbleby.net/scanline-flood-fill/
    private fun floodfillmulti(startIdx: Iterable<Int>, rule: (Int) -> Boolean): List<Int> {
        val painted = Array(data.size) { false }
        val paintLocations = ArrayDeque<Int>()
        val groupList = ArrayList<Int>()

        for (i in startIdx) {
            paintLocations.push(i)
            painted[i] = true
            groupList += i
        }

        while (!paintLocations.isEmpty()) {
            val pixel = paintLocations.pop()
            val nbrs = neighbors(pixel)
            for (p in nbrs) {
                if (!painted[p] && rule(data[p])) {
                    paintLocations.push(p)
                    groupList += p
                    painted[p] = true
                }
            }
        }

        return groupList.toList()
    }

    // flood fill an area that follows a given predicate
    // TODO: more efficient algorithm, possibly http://will.thimbleby.net/scanline-flood-fill/
    private fun floodfill(startIdx: Int, rule: (Int) -> Boolean): List<Int> {
        if (!rule(data[startIdx])) {
            return listOf()
        }

        val painted = HashSet<Int>()
        val paintLocations = ArrayDeque<Int>()
        val groupList = ArrayList<Int>()

        paintLocations.push(startIdx)
        painted += startIdx
        groupList += startIdx

        while (!paintLocations.isEmpty()) {
            val pixel = paintLocations.pop()
            val nbrs = neighbors(pixel)
            for (p in nbrs) {
                if (!painted.contains(p) && rule(data[p])) {
                    paintLocations.push(p)
                    groupList += p
                    painted += p
                }
            }
        }

        return groupList.toList()
    }

    private fun isGroupSurrounded(startIdx: Int): Boolean {
        val sameColor = data[startIdx]

        val painted = HashSet<Int>()
        val checkLocations = ArrayDeque<Int>()

        checkLocations.push(startIdx)
        painted += startIdx
        var foundLiberty = false

        while (!checkLocations.isEmpty() && !foundLiberty) {
            val pixel = checkLocations.pop()
            val nbrs = neighbors(pixel)
            for (p in nbrs) {
                if (!painted.contains(p)) {
                    if (data[p] == SquareType.Empty) {
                        foundLiberty = true
                    } else if (data[p] == sameColor) {
                        checkLocations.push(p)
                        painted += p
                    }
                }
            }
        }

        return !foundLiberty
    }
}
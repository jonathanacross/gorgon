package gorgon.gobase

import java.util.*
import kotlin.math.sqrt

data class PlayResult(
    val board: GoBoard,
    val whiteStonesTaken: Int,
    val blackStonesTaken: Int
)

// Represents the board and current stones on it.
// Doesn't include any past board hash positions that
// would be used in ko/superko checks.
data class GoBoard(
    val size: Int,
    val data: IntArray,
    val hash: Long
) {
    fun clone(): GoBoard {
        return GoBoard(size, data.clone(), hash)
    }

    fun playMove(player: Player, location: Int): PlayResult {
        if (location == Location.pass) {
            return PlayResult(this.clone(), 0, 0)
        }

        val newData = data.clone()
        var newHash = hash

        val value = SquareType.playerToSquareType(player)
        val otherValue = SquareType.playerToSquareType(player.other())
        val capturedStones = getCapturedStoneList(value, location)

        newData[location] = value
        newHash = newHash xor ZobristHash.getHash(value, location)
        for (stone in capturedStones) {
            newData[stone] = SquareType.Empty
            newHash = newHash xor ZobristHash.getHash(otherValue, stone)
        }
        val newBoard = GoBoard(size, newData, newHash)

        val whiteStonesTaken = if (player == Player.Black) capturedStones.size else 0
        val blackStonesTaken = if (player == Player.White) capturedStones.size else 0

        return PlayResult(newBoard, whiteStonesTaken, blackStonesTaken)
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

    // Returns the set of location indices that are on the board.
    fun boardSquares() = boardSquaresBySize[size]

    companion object {
        const val minSize = Location.minBoardSize
        const val maxSize = Location.maxBoardSize
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
            return GoBoard(size, data, 0)
        }

        // useful for testing.  Characters other than .XO are ignored.
        fun fromString(boardString: String): GoBoard {
            val normString = boardString
                .toLowerCase()
                .replace("[^xo.]".toRegex(), "")
            val boardSize = sqrt(normString.length.toDouble()).toInt()
            val data = getEmptyBoardDataForSize(boardSize)
            var hash: Long = 0
            for (row in 0 until boardSize) {
                for (col in 0 until boardSize) {
                    val stringIdx = row * boardSize + col
                    val symbol = normString[stringIdx]
                    val boardIdx = Location.rowColToIdx(boardSize - row, col + 1)
                    when (symbol) {
                        'x' -> {
                            data[boardIdx] = SquareType.Black
                            hash = hash xor ZobristHash.getHash(data[boardIdx], boardIdx)
                        }
                        'o' -> {
                            data[boardIdx] = SquareType.White
                            hash = hash xor ZobristHash.getHash(data[boardIdx], boardIdx)
                        }
                    }
                }
            }
            return GoBoard(boardSize, data, hash)
        }
    }

    fun isLegalMove(squareType: Int, idx: Int): Boolean {
        // square must be empty (duh!)
        if (data[idx] != SquareType.Empty) {
            return false
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
        return if (!Options.ALLOW_SUICIDE && isPotentialSuicide(squareType, idx)) {
            getCapturedStoneList(squareType, idx).isNotEmpty()
        } else {
            true
        }
    }

    private fun isPotentialSuicide(value: Int, idx: Int): Boolean {
        // temporarily place the stone
        data[idx] = value

        val isSurrounded = isGroupSurrounded(idx)

        // remove the stone
        data[idx] = SquareType.Empty

        return isSurrounded
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
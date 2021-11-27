package gorgon.engine

import gorgon.gobase.*

data class Chain(val rep: Int, val elements: List<Int>)

class Utils {
    companion object {
        fun getNonBadMoves(player: Player, state: GameState): List<Int> {
            if (state.isGameOver()) {
                return listOf()
            }

            val legalMoves = state.legalMoves(player)
            val squareType = SquareType.playerToSquareType(player)

            // don't fill in our own eyes
            val nonBadMovesOnBoard =
                legalMoves.filter { idx: Int -> !Utils.isTrivialEye(squareType, idx, state.board) }

            return nonBadMovesOnBoard.ifEmpty {
                listOf(Location.pass)
            }
        }

        // Fast check to see if this is an eye for player 'squaretype'
        // if this returns true, this *is* an eye, if returns false, then
        // may/may not be (part of) an eye
        // This code is somewhat verbose, because it's optimized for speed
        fun isTrivialEye(squareType: Int, idx: Int, board: GoBoard): Boolean {
            val up = board.data[Location.up(idx)]
            val down = board.data[Location.down(idx)]
            val left = board.data[Location.left(idx)]
            val right = board.data[Location.right(idx)]

            // See if the location is surrounded on the N, E, S, W by
            // stones of the same color.
            val areNeighborsSameColor =
                (up == squareType || up == SquareType.OffBoard) &&
                        (down == squareType || down == SquareType.OffBoard) &&
                        (left == squareType || left == SquareType.OffBoard) &&
                        (right == squareType || right == SquareType.OffBoard)
            if (!areNeighborsSameColor) {
                return false
            }

            val ne = board.data[Location.northEast(idx)]
            val se = board.data[Location.southEast(idx)]
            val nw = board.data[Location.northWest(idx)]
            val sw = board.data[Location.southWest(idx)]
            val other = SquareType.opposite(squareType)

            val sameDiagCount =
                (if (ne == squareType) 1 else 0) +
                        (if (se == squareType) 1 else 0) +
                        (if (sw == squareType) 1 else 0) +
                        (if (nw == squareType) 1 else 0)

            val otherDiagCount =
                (if (ne == other) 1 else 0) +
                        (if (se == other) 1 else 0) +
                        (if (sw == other) 1 else 0) +
                        (if (nw == other) 1 else 0)

            val isEye =
                ((sameDiagCount == 3 && otherDiagCount == 1) ||
                        (otherDiagCount == 0))
            return isEye
        }

        fun isEmptyEdge(squareType: Int, idx: Int, board: GoBoard): Boolean {
            val up = board.data[Location.up(idx)]
            val down = board.data[Location.down(idx)]
            val left = board.data[Location.left(idx)]
            val right = board.data[Location.right(idx)]

            val edgeCount =
                (if (up == SquareType.OffBoard) 1 else 0) +
                        (if (down == SquareType.OffBoard) 1 else 0) +
                        (if (left == SquareType.OffBoard) 1 else 0) +
                        (if (right == SquareType.OffBoard) 1 else 0)

            if (edgeCount == 0) {
                return false
            }
            val ne = board.data[Location.northEast(idx)]
            val se = board.data[Location.southEast(idx)]
            val nw = board.data[Location.northWest(idx)]
            val sw = board.data[Location.southWest(idx)]

            val whiteBlackCount =
                (if (up == SquareType.White || up == SquareType.Black) 1 else 0) +
                        (if (down == SquareType.White || down == SquareType.Black) 1 else 0) +
                        (if (left == SquareType.White || left == SquareType.Black) 1 else 0) +
                        (if (right == SquareType.White || right == SquareType.Black) 1 else 0) +
                        (if (ne == SquareType.White || ne == SquareType.Black) 1 else 0) +
                        (if (se == SquareType.White || se == SquareType.Black) 1 else 0) +
                        (if (sw == SquareType.White || sw == SquareType.Black) 1 else 0) +
                        (if (nw == SquareType.White || nw == SquareType.Black) 1 else 0)

            return whiteBlackCount == 0
        }

        fun findStonesInAtari(player: Player, board: GoBoard): List<Int> {
            val squareType = SquareType.playerToSquareType(player)
            val allChains = findChains(board)
            val playerChains = allChains.filter { c -> board.data[c.rep] == squareType }
            val atariChains = playerChains
                .map { c -> Pair(c, getLiberties(board, c)) }
                .filter { x -> x.second.size == 1 }
                .map { x -> x.first }
            val atariStones = atariChains.flatMap { x -> x.elements }
            return atariStones
        }

        fun getConnectedGroups(board: GoBoard): DisjointSet {
            val chains = DisjointSet(board.data.size)
            for (idx in board.boardSquares()) {
                val up = Location.up(idx)
                val right = Location.right(idx)
                if (board.data[idx] == board.data[up]) chains.union(idx, up)
                if (board.data[idx] == board.data[right]) chains.union(idx, right)
            }
            return chains
        }

        fun findChains(board: GoBoard): List<Chain> {
            val chains = getConnectedGroups(board)
            return chains.repsToSets().map { x -> Chain(x.key, x.value) }
        }

        fun getLiberties(board: GoBoard, c: Chain): Set<Int> {
            return c.elements.flatMap { idx ->
                board.neighbors(idx).filter { x -> board.data[x] == SquareType.Empty }
            }.toSet()
        }

        fun sigmoid(x: Double): Double {
            return 1.0 / (1.0 + Math.exp(-x))
        }
    }
}

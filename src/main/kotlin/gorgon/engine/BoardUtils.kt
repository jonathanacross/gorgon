package gorgon.engine

import gorgon.gobase.GoBoard
import gorgon.gobase.Location
import gorgon.gobase.SquareType

class BoardUtils {
    companion object {
        // Fast check to see if this is an eye for player 'squaretype'
        // if this returns true, this *is* an eye, if returns false, then may/may not be (part of) an eye
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
    }
}

package gorgon.gobase

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GoBoardTest {
    @Test
    fun testHashEmpty() {
        val emptyBoard = GoBoard.emptyBoard(3)
        assertEquals(0, emptyBoard.hash)
    }

    @Test
    fun testHashOrderInvariant() {
        val board1 = GoBoard.emptyBoard(3)
            .playMove(Player.White, Location.rowColToIdx(1, 1)).board
            .playMove(Player.Black, Location.rowColToIdx(2, 1)).board

        val board2 = GoBoard.emptyBoard(3)
            .playMove(Player.Black, Location.rowColToIdx(2, 1)).board
            .playMove(Player.White, Location.rowColToIdx(1, 1)).board

        assertEquals(board1.hash, board2.hash)
    }

    @Test
    fun testHashUnchangedAfterKoSwap() {
        val koboard =
            """
           4 . . . .
           3 . X O .
           2 X . X O
           1 . X O .
             1 2 3 4
            """
        val board1 = GoBoard.fromString(koboard)
        val board2 = board1
            .playMove(Player.White, Location.rowColToIdx(2, 2)).board
            .playMove(Player.Black, Location.rowColToIdx(2, 3)).board

        assertEquals(board1.hash, board2.hash)
    }

    @Test
    fun testIsLegalMove() {
        val boardPos1 =
            """
           5 . . X O .
           4 . X . X O
           3 . . X O .
           2 X . . . .
           1 . X . . .
             1 2 3 4 5
        """
        val board = GoBoard.fromString(boardPos1)

        // can't play on top of a black stone
        assertEquals(false, board.isLegalMove(SquareType.White, Location.rowColToIdx(1, 2)))
        // can't play on top of a white stone
        assertEquals(false, board.isLegalMove(SquareType.White, Location.rowColToIdx(4, 5)))
        // can play on an empty square
        assertEquals(true, board.isLegalMove(SquareType.White, Location.rowColToIdx(4, 1)))

        // black can play at 1,1 (though probably not helpful)
        assertEquals(true, board.isLegalMove(SquareType.Black, Location.rowColToIdx(1, 1)))
        // white can't play at 1,1 since it's suicide
        assertEquals(false, board.isLegalMove(SquareType.White, Location.rowColToIdx(1, 1)))

        // black can play at 4,3 (just filling in)
        assertEquals(true, board.isLegalMove(SquareType.Black, Location.rowColToIdx(4, 3)))
        // white can play at 4,3 starting a ko fight even though it's suicide
        // since they are taking a piece
        assertEquals(true, board.isLegalMove(SquareType.White, Location.rowColToIdx(4, 3)))
    }
}
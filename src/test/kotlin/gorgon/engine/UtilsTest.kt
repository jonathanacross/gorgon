package gorgon.engine

import gorgon.gobase.GoBoard
import gorgon.gobase.Location
import gorgon.gobase.SquareType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UtilsTest {

    @Test
    fun testIsTrivialEye() {
        val boardString =
            """
           8 . X . X . . . .
           7 . . X . . . . .
           6 . X . X . . X X
           5 . . X . . X . X
           4 X . . . . X X O
           3 . O . O X X . .
           2 X . . X . X . .
           1 . . . X X X . .
             1 2 3 4 5 6 7 8
            """
        val board = GoBoard.fromString(boardString)
        assertEquals(true, Utils.isTrivialEye(SquareType.Black, Location.rowColToIdx(8, 3), board))
        assertEquals(true, Utils.isTrivialEye(SquareType.Black, Location.rowColToIdx(6, 3), board))
        assertEquals(true, Utils.isTrivialEye(SquareType.Black, Location.rowColToIdx(2, 5), board))
        assertEquals(false, Utils.isTrivialEye(SquareType.Black, Location.rowColToIdx(3, 1), board))
        assertEquals(false, Utils.isTrivialEye(SquareType.Black, Location.rowColToIdx(5, 7), board))
    }

    @Test
    fun testFindChains() {
        val boardString =
            """
           4 . . X O
           3 . . X O
           2 X X . O
           1 X . O .
             1 2 3 4
            """
        val board = GoBoard.fromString(boardString)
        val chains = Utils.findChains(board)
        val loc1 = Location.rowColToIdx(1,1)
        val loc2 = Location.rowColToIdx(2,1)
        val loc3 = Location.rowColToIdx(2,2)
        val chain1 = listOf(loc1, loc2, loc3)

        assertEquals(chain1, chains[loc1].elements)
    }

    @Test
    fun testGetLiberties() {
        val boardString =
            """
           4 . . . .
           3 . . O O
           2 X X X O
           1 . X O .
             1 2 3 4
            """
        val board = GoBoard.fromString(boardString)
        val chains = Utils.findChains(board)

        val blackChain = chains.filter { c -> board.data[c.rep] == SquareType.Black }
        val expectedLiberties = setOf(
            Location.rowColToIdx(1,1),
            Location.rowColToIdx(3,1),
            Location.rowColToIdx(3,2)
        )
        assertEquals(expectedLiberties, Utils.getLiberties(board, blackChain[0]))
    }
}
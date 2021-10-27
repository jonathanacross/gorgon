package gorgon.pextract

import gorgon.gobase.GoBoard
import gorgon.gobase.Location
import gorgon.gobase.Player
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PatternExtractorTest {
    @Test
    fun testExtractPattern() {
        val boardPos =
            """
           5 . . X O .
           4 . X . X O
           3 . X . O .
           2 X . . . .
           1 . O . . .
             1 2 3 4 5
        """
        val board = GoBoard.fromString(boardPos)

        val extractor = PatternExtractor(5)

        // middle of board
        val p = extractor.getPatternAt(board, Location.rowColToIdx(3,3), Player.Black)
        assertEquals("5|...O.|....X|.O.X.|OX.X.|.OX..|", p.toString())

        // side of board
        val p2 = extractor.getPatternAt(board, Location.rowColToIdx(5,2), Player.Black)
        assertEquals("5|O.X.#|X.X.#|OX..#|#####|#####|", p2.toString())
    }

    @Test
    fun testExtractPatternSymmetry() {
        val boardPos =
            """
           5 . . X . X
           4 . . . O O
           3 . . . . .
           2 . . . O O
           1 . . X . X
             1 2 3 4 5
        """
        val board = GoBoard.fromString(boardPos)

        val extractor = PatternExtractor(3)

        val p1 = extractor.getPatternAt(board, Location.rowColToIdx(5,4), Player.Black)
        val p2 = extractor.getPatternAt(board, Location.rowColToIdx(1,4), Player.Black)
        // Even though patterns are flipped on the board, they have the same value due to symmetry.
        assertEquals(p1, p2)
    }
}
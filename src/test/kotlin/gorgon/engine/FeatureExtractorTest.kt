package gorgon.engine

import gorgon.gobase.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FeatureExtractorTest {
    @Test
    fun testGetCapturedStoneCounts() {
        val boardString =
            """
           4 O O . O
           3 X X . X
           2 O O . O
           1 X X . .
             1 2 3 4
            """
        val board = GoBoard.fromString(boardString)
        val state = GameState.newGameWithBoard(board, Player.White)

        val whiteExtractor = FeatureExtractor(state, Player.White, mapOf(), null)
        assertEquals(
            0,
            whiteExtractor.getCapturedStoneCountsFeature(Location.rowColToIdx(2, 3))
        )
        assertEquals(
            3,
            whiteExtractor.getCapturedStoneCountsFeature(Location.rowColToIdx(3, 3))
        )

        val blackExtractor = FeatureExtractor(state, Player.Black, mapOf(), null)
        assertEquals(
            2,
            blackExtractor.getCapturedStoneCountsFeature(Location.rowColToIdx(2, 3))
        )
        assertEquals(
            3,
            blackExtractor.getCapturedStoneCountsFeature(Location.rowColToIdx(4, 3))
        )
    }

    @Test
    fun testGetSelfAtari() {
        val boardString =
            """
           4 . . . .
           3 . X . X
           2 . . X .
           1 . . . .
             1 2 3 4
            """
        val board = GoBoard.fromString(boardString)
        val state = GameState.newGameWithBoard(board, Player.Black)
        val whiteExtractor = FeatureExtractor(state, Player.White, mapOf(), null)

        assertEquals(1, whiteExtractor.getSelfAtari(Location.rowColToIdx(3,3)))
        assertEquals(1, whiteExtractor.getSelfAtari(Location.rowColToIdx(2,4)))
        assertEquals(0, whiteExtractor.getSelfAtari(Location.rowColToIdx(2,2)))
    }
    @Test
    fun testGetEnemyAtari() {
        val boardString =
            """
           4 . . . .
           3 . X . .
           2 . O X .
           1 . . O .
             1 2 3 4
            """
        val board = GoBoard.fromString(boardString)
        val state = GameState.newGameWithBoard(board, Player.Black)
        val whiteExtractor = FeatureExtractor(state, Player.White, mapOf(), null)

        assertEquals(1, whiteExtractor.getEnemyAtari(Location.rowColToIdx(3,3)))
        assertEquals(1, whiteExtractor.getEnemyAtari(Location.rowColToIdx(2,4)))
        assertEquals(0, whiteExtractor.getEnemyAtari(Location.rowColToIdx(3,1)))
        assertEquals(0, whiteExtractor.getEnemyAtari(Location.rowColToIdx(4,2)))
    }

    @Test
    fun testIsJump() {
        val boardString =
            """
           4 X . . X
           3 . X . X
           2 . O X .
           1 . . . .
             1 2 3 4
            """
        val board = GoBoard.fromString(boardString)
        val state = GameState.newGameWithBoard(board, Player.Black)
        val extractor = FeatureExtractor(state, Player.White, mapOf(), null)

        val black = SquareType.Black
        val white = SquareType.White

        assertEquals(false, extractor.isJump(Location.rowColToIdx(4,2), black, Location.right, 0))
        assertEquals(true, extractor.isJump(Location.rowColToIdx(4,2), black, Location.right, 1))
        assertEquals(false, extractor.isJump(Location.rowColToIdx(4,2), black, Location.right, 2))

        assertEquals(true, extractor.isJump(Location.rowColToIdx(1,1), black, Location.up, 2))
        assertEquals(false, extractor.isJump(Location.rowColToIdx(1,1), white, Location.up, 2))

        assertEquals(false, extractor.isJump(Location.rowColToIdx(2,1), black, Location.right, 0))
        assertEquals(false, extractor.isJump(Location.rowColToIdx(2,1), black, Location.right, 1))
        assertEquals(false, extractor.isJump(Location.rowColToIdx(2,1), black, Location.right, 2))
    }

    @Test
    fun testIsKnightMove() {
        val boardString =
            """
           4 . . . X
           3 . X . .
           2 X . . .
           1 O . . .
             1 2 3 4
            """
        val board = GoBoard.fromString(boardString)
        val state = GameState.newGameWithBoard(board, Player.Black)
        val extractor = FeatureExtractor(state, Player.White, mapOf(), null)
        val black = SquareType.Black

        assertEquals(true, extractor.isKnightMove(Location.rowColToIdx(4,1), black, Location.right, Location.down, 0))
        assertEquals(false, extractor.isKnightMove(Location.rowColToIdx(4,1), black, Location.right, Location.down, 1))
        assertEquals(false, extractor.isKnightMove(Location.rowColToIdx(4,1), black, Location.right, Location.down, 2))
        assertEquals(false, extractor.isKnightMove(Location.rowColToIdx(4,1), black, Location.right, Location.down, 3))

        assertEquals(true, extractor.isKnightMove(Location.rowColToIdx(2,4), black, Location.left, Location.up, 1))
        assertEquals(false, extractor.isKnightMove(Location.rowColToIdx(1,4), black, Location.left, Location.up, 2))
    }

    @Test
    fun testGetNearCorner() {
        val boardString =
            """
           9 . . . . . . . . . 
           8 . . . . . . . . . 
           7 . . x x . x x . . 
           6 . . x x . x x . . 
           5 . . . . . . . . . 
           4 . . x x . x x . . 
           3 . . x x . x x . . 
           2 . . . . . . . . . 
           1 . . . . . . . . . 
             1 2 3 4 5 6 7 8 9
            """
        val board = GoBoard.fromString(boardString)
        val state = GameState.newGameWithBoard(board, Player.Black)
        val extractor = FeatureExtractor(state, Player.White, mapOf(), null)
        val boardSize = state.board.size

        val expected = arrayListOf(
            arrayListOf(0, 0, 0, 0, 0, 0, 0, 0, 0),
            arrayListOf(0, 0, 0, 0, 0, 0, 0, 0, 0),
            arrayListOf(0, 0, 1, 1, 0, 1, 1, 0, 0),
            arrayListOf(0, 0, 1, 1, 0, 1, 1, 0, 0),
            arrayListOf(0, 0, 0, 0, 0, 0, 0, 0, 0),
            arrayListOf(0, 0, 1, 1, 0, 1, 1, 0, 0),
            arrayListOf(0, 0, 1, 1, 0, 1, 1, 0, 0),
            arrayListOf(0, 0, 0, 0, 0, 0, 0, 0, 0),
            arrayListOf(0, 0, 0, 0, 0, 0, 0, 0, 0),
        )

        for (r in 1..boardSize) {
            for (c in 1..boardSize) {
                assertEquals(
                    expected[r - 1][c - 1],
                    extractor.getNearCorner(Location.rowColToIdx(r, c))
                )
            }
        }
    }

    @Test
    fun testGetAccess() {
        val boardString =
            """
           7 . . x . . . .
           6 . . x . . . .
           5 . . x . x x x
           4 x x x . . o .
           3 . . . . . . .
           2 . . x x . . .
           1 . . o . . . .
             1 2 3 4 5 6 7
            """
        val board = GoBoard.fromString(boardString)
        val state = GameState.newGameWithBoard(board, Player.Black)
        val extractor = FeatureExtractor(state, Player.White, mapOf(), null)

        val expected = arrayListOf(
            arrayListOf(10, 10, 10, 5, 6, 7, 8),
            arrayListOf(10, 10, 10, 4, 5, 6, 7),
            arrayListOf(10, 10, 10, 3, 10, 10, 10),
            arrayListOf(10, 10, 10, 2, 1, 0, 1),
            arrayListOf(4, 3, 4, 3, 2, 1, 2),
            arrayListOf(3, 2, 10, 10, 3, 2, 3),
            arrayListOf(2, 1, 0, 1, 2, 3, 4),
        )

        for (r in 1..7) {
            for (c in 1..7) {
                assertEquals(
                    expected[7 - r][c - 1],
                    extractor.getSelfAccess(Location.rowColToIdx(r, c))
                )
            }
        }
    }
}
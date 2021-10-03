package gorgon.engine

import gorgon.gobase.GoBoard
import gorgon.gobase.Location
import gorgon.gobase.Player
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

        val whiteExtractor = FeatureExtractor(board, Player.White)
        assertEquals(
            0,
            whiteExtractor.getCapturedStoneCountsFeature(Location.rowColToIdx(2, 3))
        )
        assertEquals(
            3,
            whiteExtractor.getCapturedStoneCountsFeature(Location.rowColToIdx(3, 3))
        )

        val blackExtractor = FeatureExtractor(board, Player.Black)
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
        val whiteExtractor = FeatureExtractor(board, Player.White)

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
        val whiteExtractor = FeatureExtractor(board, Player.White)

        assertEquals(1, whiteExtractor.getEnemyAtari(Location.rowColToIdx(3,3)))
        assertEquals(1, whiteExtractor.getEnemyAtari(Location.rowColToIdx(2,4)))
        assertEquals(0, whiteExtractor.getEnemyAtari(Location.rowColToIdx(3,1)))
        assertEquals(0, whiteExtractor.getEnemyAtari(Location.rowColToIdx(4,2)))
    }
}
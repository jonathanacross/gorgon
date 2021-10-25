package gorgon.pextract

import gorgon.gobase.Location
import gorgon.gobase.Player
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SgfReaderTest {
    @Test
    fun testParseSgfSimple() {
        val sgf = """
            (;
            EV[5th Hiroshima-Aluminum Cup]
            RO[1]
            PB[Mannami Nao]
            BR[2p]
            PW[Yamamoto Kentaro]
            WR[5p]
            KM[6.5]
            RE[W+R]
            DT[2010-10-30]

            ;B[qd];W[dd];B[pp];W[dq];B[co];W[ep];B[oc];W[qj];B[ck];W[nq]
            ;B[qn];W[qg];B[jq];W[pq])
        """.trimIndent()

        val expectedPlayedGame = PlayedGame(
            boardSize = 19,
            addedWhite = listOf(),
            addedBlack = listOf(),
            moves = listOf(
                Move(Player.Black, Location.rowColToIdx(17, 4)),
                Move(Player.White, Location.rowColToIdx(4, 4)),
                Move(Player.Black, Location.rowColToIdx(16, 16)),
                Move(Player.White, Location.rowColToIdx(4, 17)),
                Move(Player.Black, Location.rowColToIdx(3, 15)),
                Move(Player.White, Location.rowColToIdx(5, 16)),
                Move(Player.Black, Location.rowColToIdx(15, 3)),
                Move(Player.White, Location.rowColToIdx(17, 10)),
                Move(Player.Black, Location.rowColToIdx(3, 11)),
                Move(Player.White, Location.rowColToIdx(14, 17)),
                Move(Player.Black, Location.rowColToIdx(17, 14)),
                Move(Player.White, Location.rowColToIdx(17, 7)),
                Move(Player.Black, Location.rowColToIdx(10, 17)),
                Move(Player.White, Location.rowColToIdx(16, 17))
            )
        )
        assertEquals(expectedPlayedGame, SgfReader.parseSgf(sgf))
    }

    @Test
    fun testParseSgfBoardSizeAndAddedStones() {
        val sgf = """
            (;
            WR[5p]
            KM[6.5]
            RE[W+R]
            SZ[5]
            AB[ab][dc][be]
            AW[ae]
            
            ;B[aa];W[bb];B[ee];W[cd];B[ea];W[db])
        """.trimIndent()

        val expectedPlayedGame = PlayedGame(
            boardSize = 5,
            addedWhite = listOf(
                Location.rowColToIdx(1, 5)
            ),
            addedBlack = listOf(
                Location.rowColToIdx(1, 2),
                Location.rowColToIdx(4, 3),
                Location.rowColToIdx(2, 5),
            ),
            moves = listOf(
                Move(Player.Black, Location.rowColToIdx(1, 1)),
                Move(Player.White, Location.rowColToIdx(2, 2)),
                Move(Player.Black, Location.rowColToIdx(5, 5)),
                Move(Player.White, Location.rowColToIdx(3, 4)),
                Move(Player.Black, Location.rowColToIdx(5, 1)),
                Move(Player.White, Location.rowColToIdx(4, 2))
            )
        )
        assertEquals(expectedPlayedGame, SgfReader.parseSgf(sgf))
    }
}

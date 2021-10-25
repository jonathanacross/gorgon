package gorgon.pextract

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PatternTest {
    @Test
    fun testPrintSmall() {
        // Pattern
        // # . O
        // # . X
        // # X O

        // encoded as #.O#.X#XO
        // black bits 100101110 = 302
        // white bits 101100101 = 357

        val pattern = Pattern(3, 302L, 357L)
        val stringEnc = pattern.toString()
        assertEquals("3|#.O|#.X|#XO|", stringEnc)
    }

    @Test
    fun testPrintBig() {
        // Pattern
        // # # # # # # #
        // # . . . . . .
        // # . . O X . .
        // # . O X O . .
        // # . O X X O .
        // # . . O X . X
        // # . . . O . .

        // encoded as ########......#..OX..#.OXO..#.OXXO.#..OX.X#...O..
        // black bits 1111111100000010001001001000100110010001011000000 = 0x1FE04491322C0
        // white bits 1111111100000010010001010100101001010010001000100 = 0x1FE048A94A444

        val pattern = Pattern(7, 0x1FE04491322C0, 0x1FE048A94A444)
        val stringEnc = pattern.toString()
        assertEquals("7|#######|#......|#..OX..|#.OXO..|#.OXXO.|#..OX.X|#...O..|", stringEnc)
    }

    @Test
    fun testGetBoardOffsets() {
        val offsets = Pattern.getBoardOffsets(5, boardUp = 10)
        val expected = intArrayOf(
            -22, -21, -20, -19, -18,  // slashes to force line breaks
            -12, -11, -10, -9, -8,  //
            -2, -1, 0, 1, 2,  //
            8, 9, 10, 11, 12,  //
            18, 19, 20, 21, 22
        )
        assertEquals(expected.toList(), offsets.toList())
    }

    @Test
    fun testGetPatternOffsets() {
        val offsets = Pattern.getPatternOffsets(5)
        val expected = arrayOf(
            intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24),
            intArrayOf(20, 21, 22, 23, 24, 15, 16, 17, 18, 19, 10, 11, 12, 13, 14, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4),
            intArrayOf(4, 9, 14, 19, 24, 3, 8, 13, 18, 23, 2, 7, 12, 17, 22, 1, 6, 11, 16, 21, 0, 5, 10, 15, 20),
            intArrayOf(0, 5, 10, 15, 20, 1, 6, 11, 16, 21, 2, 7, 12, 17, 22, 3, 8, 13, 18, 23, 4, 9, 14, 19, 24),
            intArrayOf(24, 19, 14, 9, 4, 23, 18, 13, 8, 3, 22, 17, 12, 7, 2, 21, 16, 11, 6, 1, 20, 15, 10, 5, 0),
            intArrayOf(24, 23, 22, 21, 20, 19, 18, 17, 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0),
            intArrayOf(4, 3, 2, 1, 0, 9, 8, 7, 6, 5, 14, 13, 12, 11, 10, 19, 18, 17, 16, 15, 24, 23, 22, 21, 20),
            intArrayOf(20, 15, 10, 5, 0, 21, 16, 11, 6, 1, 22, 17, 12, 7, 2, 23, 18, 13, 8, 3, 24, 19, 14, 9, 4),
        )
        for (i in 0 until 8) {
            assertEquals(expected[i].toList(), offsets[i].toList())
        }
    }
}
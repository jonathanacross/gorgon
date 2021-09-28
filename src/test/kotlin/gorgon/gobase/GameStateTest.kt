package gorgon.gobase


import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GameStateTest {
    @Test
    fun testIsLegalMoveKo() {
        val koboard =
            """
           4 . . . .
           3 . X O .
           2 X . X O
           1 . X O .
             1 2 3 4
            """
        val state = GameState.newGameWithBoard(GoBoard.fromString(koboard), Player.Black)
            .playMove(Player.White, Location.rowColToIdx(2, 2))

        // black not allowed to retake
        // Only detected if we're detecting
        assertEquals(false, state.isLegalMove(Player.Black, Location.rowColToIdx(2, 3)))
    }

    @Test
    fun IsLegalMoveSnapback() {
        // example from https://senseis.xmp.net/?Snapback
        val snapback =
            """
           7 . . . . . . .
           6 . x o o o . .
           5 . x o x x o .
           4 . . x o . o .
           3 . . x x o o .
           2 . . x . x x .
           1 . . . . . . .
             1 2 3 4 5 6 7
        """
        val state = GameState.newGameWithBoard(
            GoBoard.fromString(snapback), Player.White
        )
            .playMove(Player.Black, Location.rowColToIdx(4, 5))

        // Now, with a simple ko, white would not be able to play at 4,4
        // to retake, but in this case, the position is different, so it is allowed.
        assertEquals(true, state.isLegalMove(Player.White, Location.rowColToIdx(4, 4)))
    }

    @Test
    fun IsLegalMoveSuperKo() {
        val superko = """
            6 . O . X O .
            5 O X X X O .
            4 O . X X O .
            3 X X X O . .
            2 O O O O . .
            1 . . . . . .
              1 2 3 4 5 6
        """
        val state = GameState.newGameWithBoard(
            GoBoard.fromString(superko), Player.White
        )
            .playMove(Player.White, Location.rowColToIdx(6, 3))
            .playMove(Player.Black, Location.rowColToIdx(6, 1))

        // White should not be able to play here since it repeats a board
        // position.
        // This is only detected if the hash is big enough
        val legal = Options.KO_HASH_SIZE < 3
        assertEquals(legal, state.isLegalMove(Player.White, Location.rowColToIdx(6, 2)))
    }

    @Test
    fun IsLegalMovePinwheelSuperKo() {
        // from https://senseis.xmp.net/?PinwheelKo
        val pinwheel = """
            3 O X .
            2 . O X
            1 O X .
              1 2 3
        """
        val state = GameState.newGameWithBoard(GoBoard.fromString(pinwheel), Player.White)
            .playMove(Player.White, Location.rowColToIdx(3, 3))
            .playMove(Player.Black, Location.rowColToIdx(2, 1))
            .playMove(Player.White, Location.rowColToIdx(1, 3))
            .playMove(Player.Black, Location.rowColToIdx(3, 2))
            .playMove(Player.White, Location.rowColToIdx(1, 1))
            .playMove(Player.Black, Location.rowColToIdx(2, 3))
            .playMove(Player.White, Location.rowColToIdx(3, 1))

        // At this point, the board looks like this:
        // O X .
        // . O X
        // O . O

        // While under single ko check, black could play at 1,2,
        // this would repeat the initial position.
        // This is only detected if the hash is big enough
        val legal = Options.KO_HASH_SIZE < 8
        assertEquals(legal, state.isLegalMove(Player.Black, Location.rowColToIdx(1, 2)))

        // Black also can't play at 2,1, since this is a simple ko
        assertEquals(false, state.isLegalMove(Player.Black, Location.rowColToIdx(2, 1)))

        // (As an aside, black can't play at 3,3 since it would be suicide,
        // so black must pass.)
    }
}

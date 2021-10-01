package gorgon.gobase

import java.util.*


data class GameState(
    val board: GoBoard,
    val playerJustMoved: Player = Player.White,
    val prevMove: Int = Location.undefined,
    val prevPrevMove: Int = Location.undefined,
    val blackStonesTaken: Int = 0,
    val whiteStonesTaken: Int = 0,
    val positionHashes: Deque<Long>
) {
    // Assumes that loc is a non-pass move.
    fun isLegalMove(player: Player, loc: Int): Boolean {
        val squareType = SquareType.playerToSquareType(player)

        // First see that the move makes sense with respect to the current board.
        if (!board.isLegalMove(squareType, loc)) {
            return false
        }

        // Then check that we aren't repeating positions
        val playResult = playMove(player, loc)
        return !positionHashes.contains(playResult.board.hash)
    }

    fun legalMoves(player: Player): List<Int> {
        return board.boardSquares().filter { i -> isLegalMove(player, i) }.toList()
    }

    fun playMove(player: Player, location: Int): GameState {
        val playResult = board.playMove(player, location)
        val newPositionHashes = ArrayDeque<Long>()
        newPositionHashes.addAll(positionHashes)
        newPositionHashes.add(playResult.board.hash)
        if (newPositionHashes.size > Options.KO_HASH_SIZE) {
            newPositionHashes.removeFirst()
        }

        return GameState(
            board = playResult.board,
            playerJustMoved = player,
            prevPrevMove = prevMove,
            prevMove = location,
            whiteStonesTaken = playResult.whiteStonesTaken,
            blackStonesTaken = playResult.blackStonesTaken,
            positionHashes = newPositionHashes
        )
    }

    override fun toString(): String {
        return "to move: " + playerJustMoved.other() + "\n" + board.toString()
    }

    fun isGameOver(): Boolean = (prevMove == Location.pass && prevPrevMove == Location.pass)

    companion object {
        fun newGame(size: Int): GameState {
            val board = GoBoard.emptyBoard(size)
            return GameState(
                board = board,
                playerJustMoved = Player.White,
                prevPrevMove = Location.undefined,
                prevMove = Location.undefined,
                blackStonesTaken = 0,
                whiteStonesTaken = 0,
                positionHashes = ArrayDeque(listOf(board.hash))
            )
        }

        // useful for testing
        fun newGameWithBoard(board: GoBoard, playerJustMoved: Player): GameState {
            return GameState(
                board = board,
                playerJustMoved = playerJustMoved,
                prevPrevMove = Location.undefined,
                prevMove = Location.undefined,
                blackStonesTaken = 0,
                whiteStonesTaken = 0,
                positionHashes = ArrayDeque(listOf(board.hash))
            )
        }
    }
}

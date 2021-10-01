package gorgon.engine

import gorgon.gobase.GameState
import gorgon.gobase.Location
import gorgon.gobase.Player
import gorgon.gobase.SquareType
import kotlin.random.Random

class NoEyeEngine : Engine() {
    private val rng = Random

    private fun getNonBadMoves(player: Player, state: GameState): List<Int> {
        if (state.isGameOver()) {
            return listOf()
        }

        val board = state.board
        val legalMoves = board.legalMoves(player)
        val squareType = SquareType.playerToSquareType(player)

        // don't fill in our own eyes
        val nonBadMovesOnBoard =
            legalMoves.filter { idx: Int -> !BoardUtils.isTrivialEye(squareType, idx, state.board) }

        return if (nonBadMovesOnBoard.isEmpty()) {
            listOf(Location.pass)
        } else {
            nonBadMovesOnBoard
        }
    }

    override fun suggestMove(player: Player, state: GameState, komi: Double): Int {
        val nonBadMoves = getNonBadMoves(player, state)
        val moveIdx = rng.nextInt(nonBadMoves.size)
        return nonBadMoves[moveIdx]
    }
}

package gorgon.engine

import gorgon.gobase.Location
import gorgon.gobase.Player
import gorgon.gobase.GameState
import kotlin.random.Random

// Plays random legal moves.
class RandomEngine : Engine() {
    private val rng = Random

    override fun suggestMove(player: Player, state: GameState, komi: Double): Int {
        if (state.prevMove == Location.pass && state.prevPrevMove == Location.pass) {
            // if two passes in a row already, not sure what else to do but pass
            return Location.pass
        } else {
            val legalMoves = state.board.legalMoves(player)

            val moveIdx = rng.nextInt(legalMoves.size + 1)
            if (moveIdx == legalMoves.size)
                return Location.pass // pass with uniform probability
            else {
                return legalMoves[moveIdx]
            }
        }
    }
}
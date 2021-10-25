package gorgon.engine

import gorgon.gobase.Location
import gorgon.gobase.Player
import gorgon.gobase.GameState
import kotlin.random.Random

// Plays random legal moves.
class RandomEngine : Engine() {
    private val rng = Random

    private fun getLegalMoves(player: Player, state: GameState): List<Int> {
        if (state.isGameOver()) {
            return listOf()
        }
        return state.legalMoves(player)
    }

    override fun suggestMove(player: Player, state: GameState, komi: Double): Int {
            val legalMoves = getLegalMoves(player, state)
            val moveIdx = rng.nextInt(legalMoves.size + 1)
            return if (moveIdx == legalMoves.size) {
                Location.pass // pass with uniform probability
            } else {
                legalMoves[moveIdx]
            }
        }

    override fun moveProbs(player: Player, state: GameState, komi: Double): List<Pair<Int, Double>>  {
        val legalMoves = getLegalMoves(player, state)
        val moveToScore = legalMoves.map { loc -> Pair(loc, 1.0) }
        return moveToScore
    }

    override fun detailScore(player: Player, loc: Int, state: GameState): String {
        return "equal prob if legal"
    }
}
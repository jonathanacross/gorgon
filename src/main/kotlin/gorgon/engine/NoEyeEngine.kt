package gorgon.engine

import gorgon.gobase.GameState
import gorgon.gobase.Location
import gorgon.gobase.Player
import kotlin.random.Random

class NoEyeEngine : Engine() {
    private val rng = Random

    override fun suggestMove(player: Player, state: GameState, komi: Double): Int {
        val nonBadMoves = Utils.getNonBadMoves(player, state)
        val moveIdx = rng.nextInt(nonBadMoves.size)
        return nonBadMoves[moveIdx]
    }

    override fun moveProbs(player: Player, state: GameState, komi: Double): List<Pair<Int, Double>>  {
        val nonBadMoves = Utils.getNonBadMoves(player, state)
        if (nonBadMoves.size == 1 && nonBadMoves[0] == Location.pass) {
            return listOf()
        }

        val moveToScore = nonBadMoves.map { loc -> Pair(loc, 1.0) }
        return moveToScore
    }

    override fun detailScore(player: Player, loc: Int, state: GameState): String {
        return "equal prob if legal"
    }

}

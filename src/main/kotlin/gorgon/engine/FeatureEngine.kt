package gorgon.engine

import gorgon.gobase.GameState
import gorgon.gobase.Location
import gorgon.gobase.Player
import kotlin.random.Random

class FeatureEngine() : Engine() {
    private val rng = Random

    private fun scoreLocation(
        loc: Int,
        extractor: FeatureExtractor,
        jitterValues: Boolean
    ): Double {
        // later, optimize the combination of features
        var score = (
                1.0 * extractor.getCapturedStoneCountsFeature(loc).toDouble()
                        - 0.7 * extractor.getSelfAtari(loc).toDouble()
                        + 0.7 * extractor.getEnemyAtari(loc).toDouble()
                        - 0.3 * extractor.getEmptyEdge(loc).toDouble()
                        + 0.025 * extractor.getInfluence(loc).toDouble()
                )
        if (jitterValues) {
            // jitter the values, so we don't always pick the same square if
            // scores are the same
            val jitter = rng.nextDouble() * 0.0001
            score += jitter
        }
        return score
    }

    override fun suggestMove(player: Player, state: GameState, komi: Double): Int {
        val nonBadMoves = Utils.getNonBadMoves(player, state)
        // Note: will always have at least one option, namely pass
        if (nonBadMoves.size == 1) {
            return nonBadMoves[0]
        }

        val extractor = FeatureExtractor(state.board, player)
        val moveToScore = nonBadMoves.map { loc -> Pair(loc, scoreLocation(loc, extractor, true)) }

        val best = moveToScore.maxByOrNull { x -> x.second }
        return best?.first ?: Location.pass
    }


    override fun moveProbs(
        player: Player,
        state: GameState,
        komi: Double
    ): List<Pair<Int, Double>> {
        val nonBadMoves = Utils.getNonBadMoves(player, state)
        if (nonBadMoves.size == 1 && nonBadMoves[0] == Location.pass) {
            return listOf()
        }

        val extractor = FeatureExtractor(state.board, player)
        val moveToScore = nonBadMoves.map { loc ->
            Pair(
                loc,
                Utils.sigmoid(scoreLocation(loc, extractor, false))
            )
        }

        return moveToScore
    }

}
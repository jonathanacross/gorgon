package gorgon.engine

import gorgon.gobase.GameState
import gorgon.gobase.Location
import gorgon.gobase.Player
import gorgon.pextract.Pattern
import kotlin.random.Random

class FeatureEngine(featureFileName: String) : Engine() {
    private val rng = Random
    private val featureWeights = FeatureWeightReader.readFeatureWeightFile(featureFileName)
    private val patterns: Map<Pattern, Int> = PatternReader.readPatternFile()
    val knownPatternValues = getKnownPatternValues(featureWeights)

    private fun getKnownPatternValues(featureWeights: List<FeatureWeight>): Set<Int> {
        val patternFeatures = featureWeights.find{fw -> fw.name == "pattern"}
        return patternFeatures?.weights?.keys ?: setOf()
    }

    private fun scoreLocation(
        loc: Int,
        extractor: FeatureExtractor,
        jitterValues: Boolean
    ): Double {
        var score = 0.0
        for (featureWeight in featureWeights) {
            val value = extractor.getFeature(featureWeight.name, loc, true)
            if (value == 0) {
                // 0 is the 'default' value with assumed 0 weight
                continue
            }
            val weight = featureWeight.weights[value]
            if (weight == null) {
                // assume weight is 0
                //throw Exception("no weight specified for feature " + featureWeight.name + " with value " + value)
            } else {
                score += weight
            }
        }
        if (jitterValues) {
            // Jitter the values, so we don't always pick the same square if
            // scores are the same.
            val jitter = rng.nextDouble() * 0.05
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

        val extractor = FeatureExtractor(state, player, patterns, knownPatternValues)
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

        val extractor = FeatureExtractor(state, player, patterns, knownPatternValues)
        val moveToScore = nonBadMoves.map { loc ->
            Pair(
                loc,
                Utils.sigmoid(scoreLocation(loc, extractor, false))
            )
        }

        return moveToScore
    }

    // TODO: logic is similar to scoreLocation.. worth combining?
    override fun detailScore(player: Player, loc: Int, state: GameState): String {
        val extractor = FeatureExtractor(state, player, patterns, knownPatternValues)
        val sb = StringBuilder()
        var score = 0.0
        for (featureWeight in featureWeights) {
            val value = extractor.getFeature(featureWeight.name, loc, true)
            if (value == 0) {
                sb.append(" " + featureWeight.name + "_" + value + "=0")
                continue
            }
            val weight = featureWeight.weights[value]
            if (weight == null) {
                throw Exception("no weight specified for feature " + featureWeight.name + " with value " + value)
            } else {
                sb.append(" " + featureWeight.name + "_" + value + "=" + weight)
                score += weight
            }
        }
        sb.insert(0, score.toString() + " from")
        return sb.toString()
    }

}
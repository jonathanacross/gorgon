package gorgon.engine

import gorgon.gobase.GameState
import gorgon.gobase.Player

abstract class Engine {
    abstract fun suggestMove(player: Player, state: GameState, komi: Double): Int
    abstract fun moveProbs(player: Player, state: GameState, komi: Double): List<Pair<Int, Double>>
    // explains how score is computed at the given point
    abstract fun detailScore(player: Player, loc: Int, state: GameState): String
}

class EngineFactory {
    companion object {
        val engines = mapOf(
            "random" to RandomEngine(),
            "noeye" to NoEyeEngine(),
            "feature" to FeatureEngine("features.tsv"),
            "experimental" to FeatureEngine("experimental_features.tsv"),
            "rave" to RaveEngine(),
            "mcts" to MctsEngine(FeatureEngine("features.tsv"), NoEyeEngine(), 500),
            "mcts2" to MctsEngine(NoEyeEngine(), NoEyeEngine(), 500),
        )

        fun newEngine(params: List<String>): Engine {
            val name = params[0]
            return engines.getValue(name)
        }
    }
}
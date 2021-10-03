package gorgon.engine

import gorgon.gobase.GameState
import gorgon.gobase.Player

abstract class Engine {
    abstract fun suggestMove(player: Player, state: GameState, komi: Double): Int
    abstract fun moveProbs(player: Player, state: GameState, komi: Double): List<Pair<Int, Double>>
}

class EngineFactory {
    companion object {
        val engines = mapOf(
            "random" to RandomEngine(),
            "noeye" to NoEyeEngine(),
            "feature" to FeatureEngine(),
        )

        fun newEngine(params: List<String>): Engine {
            val name = params[0]
            return engines.getValue(name)
        }
    }
}
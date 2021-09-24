package gorgon.engine

import gorgon.gobase.GameState
import gorgon.gobase.Player

abstract class Engine {
    abstract fun suggestMove(player: Player, state: GameState, komi: Double): Int
}

class EngineFactory {
    companion object {
        private val engines = mapOf("random" to RandomEngine())

        fun newEngine(params: List<String>): Engine {
            val name = params[0]
            return engines.getValue(name)
        }
    }
}
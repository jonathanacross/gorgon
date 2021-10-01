package gorgon.engine

import gorgon.gobase.GameState
import gorgon.gobase.Player

abstract class Engine {
    abstract fun suggestMove(player: Player, state: GameState, komi: Double): Int
}

class EngineFactory {
    companion object {
        val engines = mapOf(
            "random" to RandomEngine(),
            "noeye" to NoEyeEngine()
        )

        fun newEngine(params: List<String>): Engine {
            val name = params[0]
            return engines.getValue(name)
        }
    }
}
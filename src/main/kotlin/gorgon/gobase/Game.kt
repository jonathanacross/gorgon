package gorgon.gobase

import java.util.ArrayDeque


// State for the GTP client
class Game(val size: Int = 19, var komi: Double = 0.0) {
    private val states = ArrayDeque<GameState>()
    init {
        newGame()
    }

    fun newGame() {
        states.clear()
        states.push(GameState.newGame(size))
    }

    fun currState(): GameState {
        return states.peek()
    }
    fun currBoard(): GoBoard {
        return currState().board
    }

    fun playMove(player: Player, location: Int) {
        return states.push(currState().playMove(player, location))
    }

    fun undoMove(): GameState {
        return states.pop()
    }
}

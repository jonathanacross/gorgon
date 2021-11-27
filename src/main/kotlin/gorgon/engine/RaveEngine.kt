package gorgon.engine

import gorgon.gobase.GameState
import gorgon.gobase.Location
import gorgon.gobase.Player

class RaveEngine : Engine() {
    private val noEyeEngine = NoEyeEngine()
    private val numGames = 1000

    data class Move(val player: Player, val loc: Int)

    data class WinLoss(val wins: Int, val total: Int)

    private fun simulateGames(
        player: Player,
        state: GameState,
        komi: Double
    ): List<Pair<Int, Double>> {
        val moveToWinLoss = HashMap<Move, WinLoss>()
        val maxMovesPerGame = state.board.size * state.board.size * 3

        for (games in 1..numGames) {
            var currState = state
            var currPlayer = player
            var numMoves = 0
            val moveSet = HashSet<Move>()

            while (!currState.isGameOver() && numMoves < maxMovesPerGame) {
                val nextLoc = noEyeEngine.suggestMove(currPlayer, currState, komi)
                val nextMove = Move(currPlayer, nextLoc)
                moveSet.add(nextMove)

                currState = currState.playMove(currPlayer, nextLoc)
                currPlayer = currPlayer.other()
                numMoves++
            }
            val (b, w) = currState.board.score()
            val score = b - (w + komi)

            val isWinForCurrentPlayer = if (player == Player.Black) {
                score > 0
            } else {
                score < 0
            }
            val winDelta = if (isWinForCurrentPlayer) 1 else 0

            for (move in moveSet) {
                val prevWL = moveToWinLoss.getOrDefault(move, WinLoss(0, 0))
                val newWL = WinLoss(prevWL.wins + winDelta, prevWL.total + 1)
                moveToWinLoss[move] = newWL
            }
        }

        val nonBadMovesLocs = Utils.getNonBadMoves(player, state).toSet()
        val scoresForThisSide = moveToWinLoss
            .filter { x -> x.key.player == player }
            .filter { x -> nonBadMovesLocs.contains(x.key.loc) }
            .map { x -> Pair(x.key.loc, x.value.wins.toDouble() / x.value.total) }
            .toList()
        return scoresForThisSide
    }

    override fun suggestMove(player: Player, state: GameState, komi: Double): Int {
        val moveProbs = simulateGames(player, state, komi)
        val best = moveProbs.maxByOrNull { x -> x.second }
        return best?.first ?: Location.pass
    }

    override fun moveProbs(
        player: Player,
        state: GameState,
        komi: Double
    ): List<Pair<Int, Double>> {
        return simulateGames(player, state, komi)
    }

    override fun detailScore(player: Player, loc: Int, state: GameState): String {
        return "to implement"
    }
}
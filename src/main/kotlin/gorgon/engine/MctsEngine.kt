package gorgon.engine

import gorgon.gobase.GameState
import gorgon.gobase.Location
import gorgon.gobase.Player
import kotlin.random.Random

class GameTreeNode(
    val move: Int, // move that got us to this node
    val parent: GameTreeNode?,
    val state: GameState,
    val engine: Engine,
    val rolloutEngine: Engine,
    val heuristicScore: Double,
    // for debugging only.  Eventually remove
    val depth: Int,
    val debugName: String
) {

    var numSims: Double = 0.0
    var numWins: Double = 0.0
    val children = ArrayList<GameTreeNode>()

    val playerJustMoved = state.playerJustMoved

    val untriedMoves = computeUntriedMoves()

    private fun computeUntriedMoves(): MutableList<Pair<Int, Double>> {
        val epsilon = 0.00001
        // TODO: assuming komi = 0.0 for untried moves; komi should be handled better here
        val allUntriedMoves = engine.moveProbs(playerJustMoved.other(), state, 0.0)
        val allUntriedMovesJitter = allUntriedMoves.map { (loc, score) ->
            Pair(loc, score + rng.nextDouble() * epsilon)
        }
        val allUntriedMovesOrdered = allUntriedMovesJitter.sortedBy { x -> -x.second }
        return allUntriedMovesOrdered.toMutableList()
    }

    val priorWins = 1.0
    val priorSims = 2.0

    override fun toString(): String {
        return toStringIndented(0)
    }

    private fun toStringIndented(indent: Int): String {
        val spaces = "  ".repeat(indent)
        val sb = StringBuilder()
        sb.append(spaces + "( " + depth + " " + playerJustMoved.toString() + " " + debugName + "   wins: " + numWins + "   sims: " + numSims)
        for (c in children) {
            sb.append("\n" + c.toStringIndented(indent + 1))
        }
        sb.append(" )")
        return sb.toString()
    }

    fun selectChild(): GameTreeNode {
        val explorationFactor = 2.0
        val childSorted = children.sortedBy { child ->
            val winRate = (child.numWins + priorWins) / (child.numSims + priorSims)
            val explorationBonus = explorationFactor * child.heuristicScore / (1 + child.numSims)
            //val explorationBonus = sqrt(2.0 * ln(this.numSims) / child.numSims)
            winRate + explorationBonus
        }
        return childSorted.last()
    }

    // remove m from untriedMoves and add a new child node for it
    fun addChild(m: Int, state: GameState, heuristicScore: Double): GameTreeNode {
        val n = GameTreeNode(
            move = m,
            parent = this,
            state = state,
            engine = this.engine,
            rolloutEngine = this.rolloutEngine,
            heuristicScore = heuristicScore,
            depth = this.depth + 1,
            debugName = Location.idxToString(m)
        )

        //untriedMoves -= m
        untriedMoves.removeAt(0)
        children.add(n)
        return n
    }

    // update the node.  result is the game result from the viewpoint of playerJustMoved
    fun update(result: Double) {
        numWins += result
        numSims += 1
    }

    companion object {
        val rng = Random
    }
}

class MctsEngine(val engine: Engine, val rolloutEngine: Engine, val numGamesToSimulate: Int) : Engine() {

    fun simulate(root: GameTreeNode, komi: Double) {
        var s = root.state
        var n = root

//        println("simulating @ root = " + s.toString())

        // select
        while (n.untriedMoves.isEmpty() && !n.children.isEmpty()) // node is fully expanded and non-terminal
        {
            n = n.selectChild()
//            println("selecting " + Location.idxToString(n.move))
            s = s.playMove(n.playerJustMoved, n.move)
//            println(s)
        }

        // expand
        if (!n.untriedMoves.isEmpty()) // if state/node is non-terminal
        {
            //val moveIdx = rng.nextInt(n.untriedMoves.size)
            val m = n.untriedMoves[0].first
            val heuristicScore = n.untriedMoves[0].second
//            println("expanding " + Location.idxToString(m))
            s = s.playMove(n.playerJustMoved.other(), m)
            n = n.addChild(m, s, heuristicScore) // add child and descend
        }

        // rollout
        var numMoves = 0
        val maxMoves = s.board.size * s.board.size * 3
        while (!s.isGameOver() && numMoves < maxMoves) {
            val p = s.playerJustMoved.other()
            val m = engine.suggestMove(p, s, komi)
//            println("rollout " + Location.idxToString(m))
            s = s.playMove(p, m)
            numMoves += 1
        }

        // backprop
        var currNode: GameTreeNode? = n
        var gameResult = getGameResult(s, n.playerJustMoved, komi)
        while (currNode != null) {
            currNode.update(gameResult)
//            println("backprop " + Location.idxToString(n.move))
            currNode = currNode.parent
            gameResult = 1 - gameResult // reverse score for other player
        }
    }

    private fun getGameResult(state: GameState, playerJustMoved: Player, komi: Double): Double {
        val (b, w) = state.board.score()
        val score = if (playerJustMoved == Player.White) {
            (w + komi) - b
        } else {
            b - (w + komi)
        }
        return if (score > 0) 1.0
        else if (score < 0) 0.0
        else 0.5
    }

    override fun suggestMove(player: Player, state: GameState, komi: Double): Int {
        val goodMoves = engine.moveProbs(player, state, komi)
        if (goodMoves.isEmpty()) {
            return Location.pass
        } else if (goodMoves.size == 1) {
            return goodMoves[0].first
        } else {
            val root = runSimulation(state, komi)

            // Take the node with the maximum number of simulations
            // This is less noisy than picking the node with the
            // highest win rate.
            val best = root.children.maxByOrNull { c -> c.numSims }
            if (best == null) {
                return Location.pass
            } else {
                return best.move
            }
        }
    }

    override fun moveProbs(
        player: Player,
        state: GameState,
        komi: Double): List<Pair<Int, Double>> {

        val root = runSimulation(state, komi)
        val counts = root.children.map { x -> Pair(x.move, x.numSims) }
        val m = counts.map { x -> x.second }.maxOrNull()
        if (m == null) {
            return listOf()
        } else {
            val probs = counts.map { x -> Pair(x.first, x.second / m) }
            return probs.toList()
        }
    }

    fun runSimulation(
        state: GameState,
        komi: Double
    ): GameTreeNode {
        val root = GameTreeNode(
            move = Location.undefined,
            parent = null,
            state = state,
            engine = engine,
            rolloutEngine = rolloutEngine,
            heuristicScore = 0.0,
            // for debugging only.  Eventually remove
            depth = 0,
            debugName = "root"
        )

        // TODO: expose this
        val maxThoughtDuration = 5 * 1000
        val startTime = System.currentTimeMillis()

        //println(root)
        for (i in 0 until numGamesToSimulate) {
            //println(i)
            simulate(root, komi)
            val currTime = System.currentTimeMillis()
            if (currTime - startTime > maxThoughtDuration) {
                break
            }
            //println(root)
        }

        return root
    }

    override fun detailScore(player: Player, loc: Int, state: GameState): String {
        return "not implemented"
    }
}
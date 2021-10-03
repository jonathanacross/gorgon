package gorgon.engine

import gorgon.gobase.GoBoard
import gorgon.gobase.Location
import gorgon.gobase.Player
import gorgon.gobase.SquareType
import kotlin.math.abs

class FeatureExtractor(val board: GoBoard, val player: Player) {
    private val capturedStoneCounts = IntArray(Location.numLocs) { 0 }
    private val influenceData = Array(board.size, { IntArray(board.size) })

    init {
        val squareType = SquareType.playerToSquareType(player)
        val otherSquareType = SquareType.opposite(squareType)
        val allChains = Utils.findChains(board)

        val playerChains = allChains.filter { c -> board.data[c.rep] == otherSquareType }
        val playerLibertyAndNumStonesInAtari = playerChains
            .map { c -> Pair(Utils.getLiberties(board, c), c.elements.size) }
            .filter { x -> x.first.size == 1 }
            .map { x -> Pair(x.first.first(), x.second) }
        for (locAndNumStones in playerLibertyAndNumStonesInAtari) {
            capturedStoneCounts[locAndNumStones.first] += locAndNumStones.second
        }

        computeInfluence()
    }

    fun computeInfluence() {
        val influenceConv = arrayOf(
            arrayOf(0, 0, 0, 1, 0, 0, 0),
            arrayOf(0, 1, 2, 5, 2, 1, 0),
            arrayOf(0, 1, 6, 13, 6, 1, 0),
            arrayOf(1, 5, 13, 60, 13, 5, 1),
            arrayOf(0, 1, 6, 13, 6, 1, 0),
            arrayOf(0, 1, 2, 5, 2, 1, 0),
            arrayOf(0, 0, 0, 1, 0, 0, 0),
        )

        val squareType = SquareType.playerToSquareType(player)
        val otherSquareType = SquareType.opposite(squareType)

        for (row in 0 until board.size) {
            for (col in 0 until board.size) {
                for (dRow in -3..3) {
                    for (dCol in -3..3) {
                        val offsetRow = row + dRow
                        val offsetCol = col + dCol
                        if (offsetRow < 0 || offsetRow >= board.size ||
                            offsetCol < 0 || offsetCol >= board.size
                        ) {
                            continue
                        }
                        val influence = influenceConv[dRow + 3][dCol + 3]
                        val weight =
                            when (board.data[Location.rowColToIdx(offsetRow + 1, offsetCol + 1)]) {
                                squareType -> 1
                                otherSquareType -> -1
                                else -> 0
                            }
                        influenceData[row][col] += weight * influence
                    }
                }
            }
        }
    }

    fun getCapturedStoneCountsFeature(idx: Int): Int {
        val nStones = capturedStoneCounts[idx]
        return if (nStones < 7) nStones else 7
    }

    fun getSelfAtari(idx: Int): Int {
        val nextBoard = board.playMove(player, idx).board
        val squareType = SquareType.playerToSquareType(player)
        val group = nextBoard.floodfill(idx, { x: Int -> x == squareType })
        val chain = Chain(idx, group)

        val numLiberties = Utils.getLiberties(nextBoard, chain).size

        return if (numLiberties == 1) 1 else 0
    }

    fun getEnemyAtari(idx: Int): Int {
        val nextBoard = board.playMove(player, idx).board
        val squareType = SquareType.playerToSquareType(player)
        val otherSquareType = SquareType.opposite(squareType)
        var enemyAtariCount = 0
        for (n in nextBoard.neighbors(idx)) {
            if (nextBoard.data[n] != otherSquareType) {
                continue
            }
            val group = nextBoard.floodfill(n, { x: Int -> x == otherSquareType })
            val chain = Chain(idx, group)
            // Note: it's possible that we're overestimating the chains,
            // when two of the neighbors belong to the same chain
            val numLiberties = Utils.getLiberties(nextBoard, chain).size
            if (numLiberties == 1) {
                enemyAtariCount++
            }
        }

        return if (enemyAtariCount > 0) 1 else 0
    }

    fun getEmptyEdge(idx: Int): Int {
        val squareType = SquareType.playerToSquareType(player)
        return if (Utils.isEmptyEdge(squareType, idx, board)) 1 else 0
    }

    fun getInfluence(idx: Int): Int {
        val (r, c) = Location.idxToRowCol(idx)
        val rawInfluence = abs(influenceData[r - 1][c - 1])
        // bucket to discrete feature
        return if (rawInfluence < 1) {
            4
        } else if (rawInfluence < 3) {
            3
        } else if (rawInfluence < 9) {
            2
        } else if (rawInfluence < 27) {
            1
        } else {
            0
        }
    }

    // Use for debugging only
    fun getFeature(featureName: String, loc: Int): Int {
        return when (featureName) {
            "capturedStonesCount" -> getCapturedStoneCountsFeature(loc)
            "selfAtari" -> getSelfAtari(loc)
            "enemyAtari" -> getEnemyAtari(loc)
            "emptyEdge" -> getEmptyEdge(loc)
            "influence" -> getInfluence(loc)
            else -> 0
        }
    }
}
package gorgon.engine

import gorgon.gobase.GameState
import gorgon.gobase.Location
import gorgon.gobase.Player
import gorgon.gobase.SquareType
import gorgon.pextract.Pattern
import gorgon.pextract.PatternExtractor
import kotlin.math.abs

class FeatureExtractor(
    val state: GameState,
    val player: Player,
    val patterns: Map<Pattern, Int>,
    val knownPatternValues: Set<Int>?
) {
    private val capturedStoneCounts = IntArray(Location.numLocs) { 0 }
    private val influenceData = Array(state.board.size, { IntArray(state.board.size) })
    private val saveSelfAtariData: IntArray
    private val patternExtractor3 = PatternExtractor(3)
    private val patternExtractor5 = PatternExtractor(5)
    private val patternExtractor7 = PatternExtractor(7)

    init {
        val squareType = SquareType.playerToSquareType(player)
        val otherSquareType = SquareType.opposite(squareType)
        val allChains = Utils.findChains(state.board)

        val playerChains = allChains.filter { c -> state.board.data[c.rep] == otherSquareType }
        val playerLibertyAndNumStonesInAtari = playerChains
            .map { c -> Pair(Utils.getLiberties(state.board, c), c.elements.size) }
            .filter { x -> x.first.size == 1 }
            .map { x -> Pair(x.first.first(), x.second) }
        for (locAndNumStones in playerLibertyAndNumStonesInAtari) {
            capturedStoneCounts[locAndNumStones.first] += locAndNumStones.second
        }

        computeInfluence()
        saveSelfAtariData = computeSaveAtariData()
    }

    private fun computeInfluence() {
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

        for (row in 0 until state.board.size) {
            for (col in 0 until state.board.size) {
                for (dRow in -3..3) {
                    for (dCol in -3..3) {
                        val offsetRow = row + dRow
                        val offsetCol = col + dCol
                        if (offsetRow < 0 || offsetRow >= state.board.size ||
                            offsetCol < 0 || offsetCol >= state.board.size
                        ) {
                            continue
                        }
                        val influence = influenceConv[dRow + 3][dCol + 3]
                        val weight =
                            when (state.board.data[Location.rowColToIdx(
                                offsetRow + 1,
                                offsetCol + 1
                            )]) {
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

    private fun computeSaveAtariData(): IntArray {
        val squareType = SquareType.playerToSquareType(player)
        val allChains = Utils.findChains(state.board)

        // Find places where player has stones in atari, and where to save them.
        val playerChains = allChains.filter { c -> state.board.data[c.rep] == squareType }
        val locationsOfLibertiesForStonesInAtari = playerChains
            .map { c -> Pair(Utils.getLiberties(state.board, c), c.elements.size) }
            .filter { x -> x.first.size == 1 }
            .map { x -> x.first.first() }

        val forcedSavingLocations = IntArray(state.board.data.size) { 0 }
        for (loc in locationsOfLibertiesForStonesInAtari) {
            val nextBoard = state.board.playMove(player, loc).board
            val group = nextBoard.floodfill(loc, { x: Int -> x == squareType })
            val chain = Chain(loc, group)
            val numLiberties = Utils.getLiberties(nextBoard, chain).size
            if (numLiberties > 1) {
                forcedSavingLocations[loc] = 1
            }
        }

        return forcedSavingLocations
    }

    fun getCapturedStoneCountsFeature(loc: Int): Int {
        val nStones = capturedStoneCounts[loc]
        return if (nStones < 7) nStones else 7
    }

    // See if we can save some of our stones that are in atari
    fun getSaveSelfAtari(loc: Int): Int {
        return saveSelfAtariData[loc]
    }

    fun getSelfAtari(loc: Int): Int {
        val nextBoard = state.board.playMove(player, loc).board
        val squareType = SquareType.playerToSquareType(player)
        val group = nextBoard.floodfill(loc, { x: Int -> x == squareType })
        val chain = Chain(loc, group)

        val numLiberties = Utils.getLiberties(nextBoard, chain).size

        return if (numLiberties == 1) 1 else 0
    }

    fun getEnemyAtari(loc: Int): Int {
        val nextBoard = state.board.playMove(player, loc).board
        val squareType = SquareType.playerToSquareType(player)
        val otherSquareType = SquareType.opposite(squareType)
        var enemyAtariCount = 0
        for (n in nextBoard.neighbors(loc)) {
            if (nextBoard.data[n] != otherSquareType) {
                continue
            }
            val group = nextBoard.floodfill(n, { x: Int -> x == otherSquareType })
            val chain = Chain(loc, group)
            // Note: it's possible that we're overestimating the chains,
            // when two of the neighbors belong to the same chain
            val numLiberties = Utils.getLiberties(nextBoard, chain).size
            if (numLiberties == 1) {
                enemyAtariCount++
            }
        }

        return if (enemyAtariCount > 0) 1 else 0
    }

    fun getEmptyEdge(loc: Int): Int {
        val squareType = SquareType.playerToSquareType(player)
        return if (Utils.isEmptyEdge(squareType, loc, state.board)) 1 else 0
    }

    // Uses the distance function Dist(dx, dy) = |dx| + |dy| + max(|dx|, |dy|)
    // Range of feature is 2..17
    fun getDistToLastMove(loc: Int): Int {
        if (state.prevMove == Location.pass || state.prevMove == Location.undefined) {
            return 17
        }
        val (oldRow, oldCol) = Location.idxToRowCol(state.prevMove)
        val (row, col) = Location.idxToRowCol(loc)
        val dx = abs(oldCol - col)
        val dy = abs(oldRow - row)
        val dist = dx + dy + maxOf(dx, dy)
        return minOf(dist, 17)
    }

    fun getInfluence(loc: Int): Int {
        val (r, c) = Location.idxToRowCol(loc)
        val rawInfluence = influenceData[r - 1][c - 1]

        // bucket to discrete feature
        val breakPoints = listOf(-64, -32, -16, -8, -4, -2, -1, 0, 1, 3, 7, 15, 31, 63)
        for (i in breakPoints.indices) {
            if (rawInfluence <= breakPoints[i]) {
                return i + 1
            }
        }
        return breakPoints.size + 1
    }

    fun isJump(loc: Int, color: Int, dir: Int, numSpaces: Int): Boolean {
        var spaceCount = 0
        var currLoc = loc
        for (len in 0 until numSpaces) {
            currLoc += dir
            if (state.board.data[currLoc] != SquareType.Empty) {
                return false
            }
            spaceCount++
        }
        if (state.board.data[currLoc + dir] != color) {
            return false
        }
        return true
    }

    fun isKnightMove(loc: Int, color: Int, longDir: Int, shortDir: Int, numSpaces: Int): Boolean {
        var spaceCount = 0
        var currLoc = loc
        if (state.board.data[currLoc + shortDir] != SquareType.Empty) {
            return false
        }
        for (len in 0 until numSpaces) {
            currLoc += longDir
            if (state.board.data[currLoc] != SquareType.Empty ||
                state.board.data[currLoc + shortDir] != SquareType.Empty)  {
                return false
            }
            spaceCount++
        }
        if (state.board.data[currLoc + longDir] != SquareType.Empty ||
            state.board.data[currLoc + longDir + shortDir] != color) {
            return false
        }
        return true
    }

    private fun hasJump(loc: Int, color: Int, numSpaces: Int): Boolean {
        return isJump(loc, color, Location.left, numSpaces) ||
                isJump(loc, color, Location.right, numSpaces) ||
                isJump(loc, color, Location.up, numSpaces) ||
                isJump(loc, color, Location.down, numSpaces)
    }

    private fun hasKnightMove(loc: Int, color: Int, numSpaces: Int): Boolean {
        return isKnightMove(loc, color, Location.left, Location.up, numSpaces) ||
                isKnightMove(loc, color, Location.right, Location.up, numSpaces) ||
                isKnightMove(loc, color, Location.left, Location.down, numSpaces) ||
                isKnightMove(loc, color, Location.right, Location.down, numSpaces) ||
                isKnightMove(loc, color, Location.up, Location.left, numSpaces) ||
                isKnightMove(loc, color, Location.down, Location.left, numSpaces) ||
                isKnightMove(loc, color, Location.up, Location.right, numSpaces) ||
                isKnightMove(loc, color, Location.down, Location.right, numSpaces)
    }

    // Jumps and knight moves, as described in
    // https://econcs.seas.harvard.edu/files/econcs/files/harrisonthesis.pdf
    fun getJumps(loc: Int): Int {
        if (state.board.data[loc] != SquareType.Empty) { return 0 }

        val squareType = SquareType.playerToSquareType(player)
        if (hasJump(loc, squareType, 0)) { return 1 }
        if (hasKnightMove(loc, squareType, 0)) { return 2 }
        if (hasJump(loc, squareType, 1)) { return 3 }
        if (hasKnightMove(loc, squareType, 1)) { return 4 }
        if (hasJump(loc, squareType, 2)) { return 5 }
        if (hasKnightMove(loc, squareType, 2)) { return 6 }
        if (hasJump(loc, squareType, 3)) { return 7 }
        if (hasKnightMove(loc, squareType, 3)) { return 8 }
        if (hasJump(loc, squareType, 4)) { return 9 }
        if (hasKnightMove(loc, squareType, 4)) { return 10 }

        return 0
    }

    fun getPattern(loc: Int): Int {
        val pat7 = patternExtractor7.getPatternAt(state.board, loc, player)
        var value = patterns[pat7]
        if (value != null && (knownPatternValues == null || knownPatternValues.contains(value))) return value

        val pat5 = patternExtractor5.getPatternAt(state.board, loc, player)
        value = patterns[pat5]
        if (value != null && (knownPatternValues == null || knownPatternValues.contains(value))) return value

        val pat3 = patternExtractor3.getPatternAt(state.board, loc, player)
        value = patterns[pat3]
        if (value != null && (knownPatternValues == null || knownPatternValues.contains(value))) return value

        return 0
    }

    // Use for debugging only
    fun getFeature(featureName: String, loc: Int, dieIfUnknown: Boolean): Int {
        return when (featureName) {
            "capturedStoneCount" -> getCapturedStoneCountsFeature(loc)
            "saveSelfAtari" -> getSaveSelfAtari(loc)
            "selfAtari" -> getSelfAtari(loc)
            "enemyAtari" -> getEnemyAtari(loc)
            "emptyEdge" -> getEmptyEdge(loc)
            "influence" -> getInfluence(loc)
            "distToLastMove" -> getDistToLastMove(loc)
            "pattern" -> getPattern(loc)
            "jumps" -> getJumps(loc)
            else -> {
                if (dieIfUnknown) {
                    throw Exception("tried to get unknown feature '" + featureName + "'")
                } else {
                    return 0
                }
            }
        }
    }
}
package gorgon.gobase

import kotlin.random.Random

class ZobristHash {
    companion object {
        private val rng = Random.Default
        private const val numSquareTypes = 2  // white and black
        private const val maxNumSquares = (Location.maxBoardSize + 1) * (Location.maxBoardSize + 1)
        private val hashes = Array(maxNumSquares * numSquareTypes) { rng.nextLong() }

        fun getHash(sq: Int, position: Int): Long {
            assert(sq == SquareType.White || sq == SquareType.Black)
            val offset = if (sq == SquareType.White) 0 else 1
            return hashes[position * numSquareTypes + offset]
        }
    }
}
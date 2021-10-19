package gorgon.pextract

import gorgon.gobase.Location
import gorgon.gobase.Player

// Simple SGF reader.  Doesn't handle everything, just minimal tags to support
// loading a game; no comments, move trees handled.
data class Move(val player: Player, val square: Int)
data class PlayedGame(
    val boardSize: Int,
    val addedBlack: List<Int>,
    val addedWhite: List<Int>,
    val moves: List<Move>,
    val fileName: String
)

class SgfReader {
    companion object {
        // Extracts out size from a pattern like 'SZ[13]'
        private fun parseBoardSize(sgf: String): Int {
            val boardSizePattern = Regex("""SZ\[(\d+)\]""")
            val matches = boardSizePattern.findAll(sgf)
            if (matches.count() > 0) {
                return matches.first().groupValues[1].toInt()
            }
            // If not specified, then the default is 19x19
            return 19
        }

        private fun parseLocation(alphabeticLocation: String, boardSize: Int): Int {
            val row = (alphabeticLocation[0] - 'a' + 1).toInt()
            val col = (alphabeticLocation[1] - 'a' + 1).toInt()
            if (row < 1 || col < 1 || row > boardSize || col > boardSize) {
                throw Exception("when reading SGF, found move that was outside the board; location = " + alphabeticLocation + " boardsize = " + boardSize)
            }
            return Location.rowColToIdx(row, col)
        }

        // Parses a string like 'W[cd]' or 'B[fg]'
        private fun parseMove(moveStr: String, boardSize: Int): Move {
            val player = if (moveStr[0] == 'W') Player.White else Player.Black
            val location = parseLocation(moveStr.substring(2, 4), boardSize)
            return Move(player, location)
        }

        // Extracts out added black stones from a pattern like 'AB[ab][dc][be]'
        private fun parseAddedStones(sgf: String, black: Boolean, boardSize: Int): List<Int> {
            val addedStonesPattern = if (black) {
                Regex("""AB\[([a-z\[\]]+)\]""")
            } else {
                Regex("""AW\[([a-z\[\]]+)\]""")
            }
            val matches = addedStonesPattern.findAll(sgf)
            if (matches.count() > 0) {
                val addedStoneAlphabeticLocations = matches.first().groupValues[1]
                val stoneLocs = addedStoneAlphabeticLocations.split("][")
                    .map { x -> parseLocation(x, boardSize) }
                return stoneLocs
            }
            return listOf()
        }

        // Parses moves from a string like ';B[aa];W[bb];B[ee];W[cd];B[ea];W[db]'
        private fun parseMoves(sgf: String, boardSize: Int): List<Move> {
            val movePattern = Regex(""";([BW]\[[a-z][a-z]\])""")
            val matches = movePattern.findAll(sgf)
            val moves = matches.map { m -> m.groupValues[1] }
                .map { s -> parseMove(s, boardSize) }
                .toList()
            return moves
        }

        fun parseSgf(sgf: String, fileName: String): PlayedGame {
            val boardSize = parseBoardSize(sgf)
            val addedBlack = parseAddedStones(sgf, black = true, boardSize)
            val addedWhite = parseAddedStones(sgf, black = false, boardSize)
            val moves = parseMoves(sgf, boardSize)
            return PlayedGame(boardSize, addedBlack, addedWhite, moves, fileName)
        }
    }
}
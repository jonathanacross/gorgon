package gorgon.pextract

import gorgon.gobase.GoBoard
import gorgon.gobase.Player
import java.io.File

fun analyzeGame(game: PlayedGame, extractor: PatternExtractor): HashSet<Pattern> {

    try {
        // set up initial position
        var b = GoBoard.emptyBoard(game.boardSize)
        for (loc in game.addedBlack) {
            b = b.putStone(Player.Black, loc)
        }
        for (loc in game.addedWhite) {
            b = b.putStone(Player.White, loc)
        }

        val patternsForThisGame = HashSet<Pattern>()

        // Play out the game
        for (move in game.moves) {
            for (loc in b.legalMoves(move.player)) {
                val pattern = extractor.getPatternAt(b, loc, move.player)
                patternsForThisGame.add(pattern)
            }

            b = b.playMove(move.player, move.square).board
        }

        return patternsForThisGame
    } catch (e: Exception) {
        println("Warning: problem analyzing game " + game.fileName)
        return HashSet()
    }
}

fun readGame(file: File): PlayedGame? {
    return try {
        val text = file.readText(Charsets.UTF_8)
        val game = SgfReader.parseSgf(text, file.path)
        game
    } catch (e: Exception) {
        null
    }
}

fun readGames(dir: String): List<PlayedGame> {
    val games = File(dir).walk()
        .filter { x -> x.isFile && x.extension == "sgf" }
        .map { x -> readGame(x) }
        .filterNotNull()
    return games.toList()
}

fun main(args: Array<String>) {
    // These could be made into arguments.
    val patternSize = 7
    val gamesDir = "/home/jonathan/Development/gorgon/data/games/"
    //val gamesDir = "/home/jonathan/Development/gorgon/data/games/Honinbo/"
    val outputFile = "/home/jonathan/Development/gorgon/data/patterns_" + patternSize + "_list.txt"


    val extractor = PatternExtractor(patternSize)
    val sketch = CountMinSketch()
    val topPatterns = HashMap<Pattern, Int>()

    val games = readGames(gamesDir)
    val threshold = games.size / 50
    var count = 0
    for (game in games) {
//        println("processing " + games[count].fileName)
        count++
        if (count % 20 == 0) {
            println("processing game " + count + " of " + games.size + ". topPatterns.size = " + topPatterns.size)
        }
        val seenPatterns = analyzeGame(game, extractor)
        for (p in seenPatterns) {
            val freq = sketch.add(p)  // frequency is an approximation
            if (freq >= threshold) {
                topPatterns[p] = freq
            }
        }
    }

    println("found " + topPatterns.size + " patterns")
    //val sortedPatterns = topPatterns.toList().sortedBy{ (_, value) -> -value}

    println("writing list of patterns to " + outputFile)
    File(outputFile).printWriter().use { out ->
        for ((p, freq) in topPatterns) {
            out.println( p.size.toString() + "," + p.blackBits + "," + p.whiteBits + "\t" + p.toString() + "\t" + freq )
        }
    }

    println("Done!")
}
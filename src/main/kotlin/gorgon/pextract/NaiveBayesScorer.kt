package gorgon.pextract

import gorgon.engine.FeatureExtractor
import gorgon.engine.PatternReader
import gorgon.gobase.GameState
import gorgon.gobase.GoBoard
import gorgon.gobase.Player
import java.io.File
import java.util.*
import kotlin.math.ln

data class Feature(val name: String, val value: Int)
data class Freq(var timesWon: Long, var timesLost: Long)

class FeatureReader {
    companion object {
        private fun parseLine(line: String): Feature {
            val fields = line.split("\t")
            val name = fields[0]
            val value = fields[1].toInt()
            return Feature(name, value)
        }

        fun readFeatureFile(featureFileName: String): List<Feature> {
            val text = File(featureFileName).readText()

            val lines = text.split("\n")
            val features = lines.filterNot { line -> line.startsWith("#") || line.isBlank() }
                .map { line -> parseLine(line) }

            return features.toList()
        }
    }
}

//data class IndexedFeatureAndWeight(val fIdx: Int, val weight: Int)
//data class TrainingExample(val label: Int, val features: List<IndexedFeatureAndWeight>)
//class ExampleReader {
//    companion object {
//        private fun parseLine(line: String): TrainingExample {
//            val fields = line.split("\\s")
//            val label = fields[0].toInt()
//            val
//        }
//    }
//}

class NaiveBayesScorerUsingGames(features: List<Feature>) {
    private val patternData: Map<Pattern, Int> = PatternReader.readPatternFile()
    val featureNameToValueToFreqs = HashMap<String, HashMap<Int, Freq>>()
    val bias = Freq(0, 0)
    init {
        for (feature in features) {
            featureNameToValueToFreqs[feature.name]  = hashMapOf()
        }
    }

    fun analyzeGame(game: PlayedGame) {
        try {
            // set up initial position
            var b = GoBoard.emptyBoard(game.boardSize)
            for (loc in game.addedBlack) {
                b = b.putStone(Player.Black, loc)
            }
            for (loc in game.addedWhite) {
                b = b.putStone(Player.White, loc)
            }

            // Play out the game
            var state = GameState.newGameWithBoard(b, Player.White)
            for (move in game.moves) {
                val featureExtractor = FeatureExtractor(state, move.player, patternData, null)
                for (loc in state.board.legalMoves(move.player)) {
                    if (loc == move.square) {
                        bias.timesWon++
                    } else {
                        bias.timesLost++
                    }
                    for ((name, valueToFreqs) in featureNameToValueToFreqs) {
                        val value = featureExtractor.getFeature(name, loc, true)
                        if (value == 0) {
                            continue
                        }
                        val freqs: Freq = valueToFreqs.getOrPut(value) { Freq(0, 0) }
                        if (loc == move.square) {
                            freqs.timesWon++
                        } else {
                            freqs.timesLost++
                        }
                    }
                }

                state = state.playMove(move.player, move.square)
            }
        } catch (e: Exception) {
            println("Warning: problem analyzing game " + game.fileName)
        }
    }
}


fun main(args: Array<String>) {
    // These could be made into arguments.
    val featureListFile = "/home/jonathan/Development/gorgon/data/feature_list.txt"
    //val gamesDir = "/home/jonathan/Development/gorgon/data/games/"
    val gamesDir = "/home/jonathan/Development/gorgon/data/games/Takagawa/"
    //val gamesDir = "/home/jonathan/Development/gorgon/data/games/Masters/"
    //val patternsFile = "/home/jonathan/Development/gorgon/data/patterns_5_list.txt"
    val outputFile = "/home/jonathan/Development/gorgon/data/naive_bayes_feature_weights.txt"

    // read patterns from patternsFile
    // create map pattern -> [wins, losses]

    val features = FeatureReader.readFeatureFile(featureListFile)
    val games = readGames(gamesDir)
    val nbScorer = NaiveBayesScorerUsingGames(features)

    var count = 0
    for (game in games) {
        count++
        if (count % 10 == 0) {
            println("processing " + count + " of " + games.size)
        }
        nbScorer.analyzeGame(game)
    }

    val bias = nbScorer.bias

    val biasWeight = ln(bias.timesWon.toDouble() / bias.timesLost.toDouble())
    val winPrior = bias.timesWon.toDouble() / bias.timesLost.toDouble()
    val lossPrior = 1.0 - winPrior

    File(outputFile).printWriter().use { out ->
        out.println("_bias_" + "\t" + 1 + "\t" + biasWeight + "\t" + bias.timesWon + "\t" + bias.timesLost)
        for ((name, valFreq) in nbScorer.featureNameToValueToFreqs) {
            for ((value, freq) in valFreq) {
                if (value == 0) {
                    continue
                }
                val probFeatValGivenWin = (freq.timesWon.toDouble() + winPrior) / (bias.timesWon.toDouble() + 1.0)
                val probFeatValGivenLoss = (freq.timesLost.toDouble() + lossPrior) / (bias.timesLost.toDouble() + 1.0)
                val weight = ln(probFeatValGivenWin / probFeatValGivenLoss)
                out.println(name + "\t" + value + "\t" + weight + "\t" + freq.timesWon + "\t" + freq.timesLost)
            }
        }
    }
}


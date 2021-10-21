package gorgon.pextract

import gorgon.engine.FeatureExtractor
import gorgon.gobase.GameState
import gorgon.gobase.GoBoard
import gorgon.gobase.Location
import gorgon.gobase.Player
import java.lang.Math.log
import java.util.*
import kotlin.collections.HashSet
import kotlin.math.ln

data class Feature(val name: String, val value: Int)
data class Freq(var timesWon: Int, var timesLost: Int)

class FeatureReader {
    companion object {
        private fun parseLine(line: String): Feature {
            val fields = line.split("\t")
            val name = fields[0]
            val value = fields[1].toInt()
            return Feature(name, value)
        }

        fun readFeatureFile(featureFileName: String): List<Feature> {
            val path = "/"  // relative to src/main/resources
            val text = FeatureReader::class.java.getResource(path + featureFileName).readText()

            val lines = text.split("\n")
            val features = lines.filterNot { line -> line.startsWith("#") || line.isBlank() }
                .map { line -> parseLine(line) }

            return features.toList()
        }

    }
}

class NaiveBayesScorer(val features: List<Feature>) {

    val featureNameToValueToFreqs = HashMap<String, HashMap<Int, Freq>>()
    val bias = Freq(1, 1)
    init {
        for (feature in features) {
            featureNameToValueToFreqs[feature.name]  = hashMapOf()
        }
    }

    fun analyzeGame(game: PlayedGame) {
//        try {
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
                val featureExtractor = FeatureExtractor(state, move.player)
                for (loc in state.board.legalMoves(move.player)) {
                    for ((name, valueToFreqs) in featureNameToValueToFreqs) {
                        val value = featureExtractor.getFeature(name, loc, true)
                        if (value == 0) {
                            continue
                        }
                        val freqs: Freq = valueToFreqs.getOrPut(value) { Freq(1, 1) }
                        if (loc == move.square) {
                            freqs.timesWon++
                            bias.timesWon++
                        } else {
                            freqs.timesLost++
                            bias.timesLost++
                        }
                    }
                }

                state = state.playMove(move.player, move.square)
            }
//        } catch (e: Exception) {
//            println("Warning: problem analyzing game " + game.fileName)
//        }
    }
}


fun main(args: Array<String>) {
    // These could be made into arguments.
    //val gamesDir = "/home/jonathan/Development/gorgon/data/games/"
    val gamesDir = "/home/jonathan/Development/gorgon/data/games/Takagawa/"
    //val gamesDir = "/home/jonathan/Development/gorgon/data/games/Masters/"
    //val patternsFile = "/home/jonathan/Development/gorgon/data/patterns_5_list.txt"

    // read patterns from patternsFile
    // create map pattern -> [wins, losses]

    val features = FeatureReader.readFeatureFile("handcrafted_features.tsv")
    val games = readGames(gamesDir)
    val nbScorer = NaiveBayesScorer(features)

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
    println("_bias_" + "\t" + 1 + "\t" + biasWeight + "\t" + bias.timesWon + "\t" + bias.timesLost)
    for ((name, valFreq) in nbScorer.featureNameToValueToFreqs) {
        for ((value, freq) in valFreq) {
            if (value == 0) {
                continue
            }
            val probFeatValGivenWin = freq.timesWon.toDouble() / bias.timesWon.toDouble()
            val probFeatValGivenLoss = freq.timesLost.toDouble() / bias.timesLost.toDouble()
            val weight = ln( probFeatValGivenWin / probFeatValGivenLoss)
            println(name + "\t" + value + "\t" + weight + "\t" + freq.timesWon + "\t" + freq.timesLost)
        }
    }
}


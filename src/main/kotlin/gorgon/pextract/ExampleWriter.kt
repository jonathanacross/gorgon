package gorgon.pextract

import gorgon.engine.FeatureExtractor
import gorgon.gobase.GameState
import gorgon.gobase.GoBoard
import gorgon.gobase.Location
import gorgon.gobase.Player
import java.io.File
import java.io.FileOutputStream
import java.lang.Math.log
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import kotlin.collections.HashSet
import kotlin.math.ln

data class FeatureIdxAndValue(val featureIdx: Int, val value: Int)
data class Example(val label: Int, val nonZeroFeatures: List<FeatureIdxAndValue>) {
    override fun toString(): String {
        return label.toString() + " " + nonZeroFeatures.map{ f -> f.featureIdx.toString() + ":" + f.value.toString()}.joinToString(" ")
    }
}

class ExampleWriter(val features: List<Feature>, val featureMapFileName: String, val examplesFileName: String) {

    data class FeatNameValueIdx(val name: String, val value: Int, val idx: Int)
    private val featureNameToValueToIdx : Map<String, Map<Int, Int>>
    init {
        val indexedFeatures = features.mapIndexed{idx, fv -> FeatNameValueIdx(fv.name, fv.value, idx)}
        featureNameToValueToIdx = indexedFeatures
            .groupBy { x -> x.name }
            .mapValues { l -> l.value.associateBy({ x -> x.value }, { x -> x.idx + 1}) }
    }

    fun writeFeatureMapFile() {
        File(featureMapFileName).printWriter().use { out ->
            out.println("#name\tvalue\tindex")
            for ((name, valueIdx) in featureNameToValueToIdx) {
                for ((value, idx) in valueIdx) {
                    out.println(name + "\t" + value + "\t" + idx)
                }
            }
        }
    }

    fun writeFeaturesForGame(game: PlayedGame) {
        val examples = ArrayList<Example>()
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
                    val label = if (loc == move.square) 1 else 0
                    val fValues = ArrayList<FeatureIdxAndValue>()
                    for ((name, valueToIdx) in featureNameToValueToIdx) {

                        val value = featureExtractor.getFeature(name, loc, true)
                        if (value == 0) {
                            continue
                        }
                        val idx = valueToIdx[value]!!
                        fValues.add(FeatureIdxAndValue(idx, 1))
                    }
                    val example = Example(label, fValues)
                    examples.add(example)

                }

                state = state.playMove(move.player, move.square)
            }
//        } catch (e: Exception) {
//            println("Warning: problem analyzing game " + game.fileName)
//        }
        FileOutputStream(examplesFileName, true).bufferedWriter().use { writer ->
            for (example in examples) {
                writer.appendLine(example.toString())
            }
        }
    }
}


fun main(args: Array<String>) {
    // These could be made into arguments.
    //val gamesDir = "/home/jonathan/Development/gorgon/data/games/"
    //val gamesDir = "/home/jonathan/Development/gorgon/data/games/Chisato/"
    val gamesDir = "/home/jonathan/Development/gorgon/data/games/Takagawa/"
    //val gamesDir = "/home/jonathan/Development/gorgon/data/games/Masters/"
    //val patternsFile = "/home/jonathan/Development/gorgon/data/patterns_5_list.txt"

    // read patterns from patternsFile
    // create map pattern -> [wins, losses]

    val features = FeatureReader.readFeatureFile("handcrafted_features.tsv")
    val games = readGames(gamesDir)

    val featureMapFileName = "/home/jonathan/Development/gorgon/data/feature_map.tsv"
    val examplesFileName = "/home/jonathan/Development/gorgon/data/examples.tsv"
    val exampleWriter  = ExampleWriter(features, featureMapFileName, examplesFileName)

    exampleWriter.writeFeatureMapFile()

    // erase the examples file before writing, since we write in append mode
    val examplesFilePath = Paths.get(examplesFileName)
    if(examplesFilePath != null && Files.exists(examplesFilePath) && Files.isRegularFile(examplesFilePath)) {
        Files.delete(examplesFilePath)
    }
    var count = 0
    for (game in games) {
        count++
        if (count % 10 == 0) {
            println("processing " + count + " of " + games.size)
        }
        exampleWriter.writeFeaturesForGame(game)
    }

    println("Wrote mapping to " + featureMapFileName)
    println("Wrote examples to " + examplesFileName)
    println("Done!")

}


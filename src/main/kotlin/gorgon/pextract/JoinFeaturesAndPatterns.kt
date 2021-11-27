package gorgon.pextract

import java.io.File
import java.io.FileOutputStream

data class PatternData(val value: Int, val patData: String, val humanData: String, val freq: String)
data class FeatureData(val name: String, val value: Int, val weight: String, val wins: String, val losses: String)

fun readPatternFile(fileName: String): Map<Int, PatternData> {
    val text = File(fileName).readText()

    val valToData = HashMap<Int, PatternData>()

    val lines = text.split("\n")
    for (line in lines) {
        if (line.startsWith("#") || line.isBlank()) {
            continue
        }

        val fields = line.split("\t")
        val value = fields[0].toInt()
        val patData = fields[1]
        val humanData = fields[2]
        val freq = fields[3]

        valToData[value] = PatternData(value, patData, humanData, freq)
    }

    return valToData
}

fun readFeatureFile(fileName: String): List<FeatureData> {
    val text = File(fileName).readText()

    val featureData = ArrayList<FeatureData>()

    val lines = text.split("\n")
    for (line in lines) {
        if (line.startsWith("#") || line.isBlank()) {
            continue
        }

        val fields = line.split("\t")
        val name = fields[0]
        val value = fields[1].toInt()
        val weight = fields[2]
        val wins = fields[3]
        val losses = fields[4]

        featureData.add(FeatureData(name, value, weight, wins, losses))
    }

    return featureData
}

fun main(args: Array<String>) {
    val patternFileName = "/Users/jonathan/Development/gorgon/src/main/resources/patterns.tsv"
    val featureFileName = "/Users/jonathan/Development/gorgon/src/main/resources/features_pat3.tsv"
    val outputFile = "/Users/jonathan/Development/gorgon/data/experimental_features_joined_debug.tsv"

    val patternData = readPatternFile(patternFileName)
    val featureData = readFeatureFile(featureFileName)

    FileOutputStream(outputFile).bufferedWriter().use { writer ->
        for (feature in featureData) {
            val columns =
                if (feature.name == "pattern") {
                    val pattern = patternData[feature.value]!!
                    listOf(
                        feature.name,
                        feature.value.toString(),
                        feature.weight,
                        pattern.humanData,
                        pattern.freq,
                        feature.wins,
                        feature.losses
                    )
                } else {
                    listOf(
                        feature.name,
                        feature.value.toString(),
                        feature.weight,
                        "",
                        "",
                        feature.wins,
                        feature.losses
                    )
                }
            writer.appendLine(columns.joinToString("\t"))
        }
    }
}

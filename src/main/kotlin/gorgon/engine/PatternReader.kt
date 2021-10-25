package gorgon.engine

import gorgon.pextract.Pattern

class PatternReader {
    companion object {
        private fun parsePattern(patternData: String) : Pattern {
            val fields = patternData.split(",")
            val patternSize = fields[0].toInt()
            val blackBits = fields[1].toLong()
            val whiteBits = fields[2].toLong()
            return Pattern(patternSize, blackBits, whiteBits)
        }

        fun readPatternFile(): Map<Pattern, Int> {
            val path = "/"  // relative to src/main/resources
            val patternFileName = "patterns.tsv"
            val text = PatternReader::class.java.getResource(path + patternFileName).readText()

            val patternToIdxMap = HashMap<Pattern, Int>()

            val lines = text.split("\n")
            for (line in lines) {
                if (line.startsWith("#") || line.isBlank()) {
                    // skip comments
                    continue
                }

                val fields = line.split("\t")
                val value = fields[0].toInt()
                val pattern = parsePattern(fields[1])

                patternToIdxMap[pattern] = value
            }

            return patternToIdxMap
        }
    }
}
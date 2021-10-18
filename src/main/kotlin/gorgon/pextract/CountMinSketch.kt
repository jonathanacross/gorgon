package gorgon.pextract

import kotlin.math.min

class CountMinSketch {
    private val numHashes = 5
    private val hashSize = 200003
    private val lookupMaps = Array(numHashes, { IntArray(hashSize) })

    fun add(pattern: Pattern) {
        for (hashIdx in 0 until numHashes) {
            val hash = pattern.countMinSketchHash(hashIdx)
            lookupMaps[hashIdx][(hash % hashSize).toInt()]++
        }
    }

    // May overestimate frequency, but never underestimate
    fun frequency(pattern: Pattern): Int {
        var minCount = Int.MAX_VALUE
        for (hashIdx in 0 until numHashes) {
            val hash = pattern.countMinSketchHash(hashIdx)
            minCount = min(minCount, lookupMaps[hashIdx][(hash % hashSize).toInt()])
        }
        return minCount
    }
}
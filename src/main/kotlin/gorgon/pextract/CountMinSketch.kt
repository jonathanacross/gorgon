package gorgon.pextract

import kotlin.math.min

class CountMinSketch {
    private val numHashes = 7
    private val hashSize = 200003
    private val lookupMaps = Array(numHashes, { IntArray(hashSize) })

    // Returns the frequency of the pattern after insertion.
    fun add(pattern: Pattern): Int {
        var minCount = Int.MAX_VALUE
        for (hashIdx in 0 until numHashes) {
            val hash = pattern.countMinSketchHash(hashIdx)
            val bucket = (hash % hashSize).toInt()
            val freq = lookupMaps[hashIdx][bucket] + 1
            minCount = min(minCount, freq)
            lookupMaps[hashIdx][bucket] = freq
        }
        return minCount
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
package gorgon.engine

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DisjointSetTest {
    @Test
    fun testFind() {
        val set = DisjointSet(6)

        // At the beginning, all elements are disjoint
        assertEquals(0, set.find(0))
        assertEquals(2, set.find(2))
        assertEquals(3, set.find(3))
        assertEquals(5, set.find(5))
    }

    @Test
    fun testRepsToSets() {
        val set = DisjointSet(6)

        // Join a few elements
        set.union(0, 1)
        set.union(2, 5)
        set.union(4, 5)

        // now has { 1 -> [0, 1], 5 -> [2, 4, 5], 3 ->[3] }

        val repsToSets = set.repsToSets()
        assertEquals(2, repsToSets[1]!!.size)
        assertEquals(3, repsToSets[5]!!.size)
        assertEquals(1, repsToSets[3]!!.size)
    }
}
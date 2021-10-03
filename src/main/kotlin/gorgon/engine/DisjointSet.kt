package gorgon.engine

// Represents a collection of disjoint sets of the integers [0, size)
class DisjointSet(val size: Int) {
    private var rank = IntArray(size) { 0 }     // rank[i] = 0
    private var parent = IntArray(size) { it }  // parent[i] = i

    // Returns the root/representative element of the set in which i belongs to.
    // e.g., if the sets are [1,2] [3,5,6], [4], then
    // find(3) = find(5) = find(6) = 3.
    fun find(i: Int): Int {
        return if (parent[i] == i) {
            i
        } else {
            val result = find(parent[i])
            // cache the result -- path compression
            parent[i] = result
            result
        }
    }

    // Combines the sets with elements i and j.
    // For example, if the sets were [1,3] [2,5], [4]
    // Then union(2,4) would result in the sets [1,3], [2,4,5]
    fun union(i: Int, j: Int) {
        // Find the representatives (or the root nodes) for each element
        val irep = find(i)
        val jrep = find(j)

        // if already in the same set, don't do anything
        if (irep != jrep) {
            // get ranks
            val irank = rank[irep]
            val jrank = rank[jrep]

            // if rank of x < rank y, then move x under y
            if (irank < jrank) {
                parent[irep] = jrep
            } else if (jrank < irank) {
                parent[jrep] = irep
            } else {
                parent[irep] = jrep
                rank[jrep] = rank[jrep] + 1
            }
        }
    }

    // Returns the set of elements for each representative.
    // For example, if the sets are [1,3], [2,5,6], [4]
    // (with representative elements 1, 2, 4, then this would return
    // {1 -> [1,3], 2 -> [2,5,6], 4 -> [4]}
    fun repsToSets(): Map<Int, List<Int>> {
        val parentsAndElems = (0 until size).map { i -> Pair(find(i), i) }
        val repsToSets = parentsAndElems.groupBy { x -> x.first }
            .mapValues { l -> l.value.map { x -> x.second }.toList() }
        return repsToSets
    }
}

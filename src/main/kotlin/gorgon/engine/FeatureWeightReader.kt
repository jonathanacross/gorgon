package gorgon.engine

data class FeatureWeight(
    val name: String,
    val weights: Map<Int, Double>, // feature value to weight
)

class FeatureWeightReader {
    companion object {
        fun readFeatureWeightFile(featureFileName: String): List<FeatureWeight> {
            val path = "/"  // relative to src/main/resources
            val text =
                FeatureWeightReader::class.java.getResource(path + featureFileName).readText()

            val nameToWeightMap = HashMap<String, HashMap<Int, Double>>()

            val lines = text.split("\n")
            for (line in lines) {
                if (line.startsWith("#") || line.isBlank()) {
                    // skip comments
                    continue
                }

                val fields = line.split("\t")
                val name = fields[0]
                val value = fields[1].toInt()
                val weight = fields[2].toDouble()

                val valToWeight = nameToWeightMap.getOrPut(name) { hashMapOf() }
                valToWeight[value] = weight
            }

            return nameToWeightMap.map { (name, valToWeight) ->
                FeatureWeight(name, valToWeight.toMap())
            }.toList()
        }
    }
}
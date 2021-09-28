package gorgon.gobase

enum class KoRules {
    KO_ALLOWED,  // nobody plays with this
    SIMPLE_KO,
    TROMP_TAYLOR
}

class Options {
    companion object {
        const val ALLOW_SUICIDE = false
        val koRule = KoRules.TROMP_TAYLOR

        // Hash size in plies to support the ko rule above
        val KO_HASH_SIZE = when (koRule) {
            KoRules.KO_ALLOWED -> 0
            KoRules.SIMPLE_KO -> 2
            KoRules.TROMP_TAYLOR -> 8
        }
    }
}
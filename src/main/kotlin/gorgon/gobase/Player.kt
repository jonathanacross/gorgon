package gorgon.gobase

sealed class Player {
    abstract fun other(): Player

    object White : Player() {
        override fun other() = Black
        override fun toString() = "W"
    }

    object Black : Player() {
        override fun other() = White
        override fun toString() = "B"
    }

    companion object {
        fun parsePlayerString(s: String): Player {
            return when (s.toUpperCase()) {
                "W" -> White
                "B" -> Black
                "WHITE" -> White
                "BLACK" -> Black
                else -> throw Exception("unknown player")
            }
        }
    }
}

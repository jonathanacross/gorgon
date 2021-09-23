package gorgon.gobase

object SquareType {
    const val Empty = 0x01
    const val White = 0x02
    const val Black = 0x04
    const val OffBoard = 0x08

    // switch black and white
    fun opposite(square: Int): Int {
        return when (square) {
            Empty -> Empty
            White -> Black
            Black -> White
            else -> OffBoard
        }
    }

    fun printForm(square: Int): String {
        return when (square) {
            Empty -> "."
            White -> "O"
            Black -> "X"
            OffBoard -> "%"
            else -> "?"
        }
    }

    fun playerToSquareType(player: Player): Int {
        return when (player) {
            Player.White -> White
            Player.Black -> Black
        }
    }
}
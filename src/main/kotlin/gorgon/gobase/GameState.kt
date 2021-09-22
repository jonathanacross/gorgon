package gorgon.gobase

data class GameState(
  val board: GoBoard,
  val playerJustMoved: Player = Player.White,
  val prevMove: Int = Location.undefined,
  val prevPrevMove: Int = Location.undefined,
  val blackStonesTaken: Int = 0,
  val whiteStonesTaken: Int = 0 // TODO: add past board hashes, so that we can detect superko
  ) {

  fun playMove(player: Player, location: Int): GameState {
    return GameState(
      board = board.play(player, location),
      playerJustMoved = player,
      prevPrevMove = prevMove,
      prevMove = location,
      blackStonesTaken = blackStonesTaken,
      whiteStonesTaken = whiteStonesTaken)
  }

  override fun toString(): String {
    return "to move: " + playerJustMoved.other() + "\n" + board.toString()
  }

  companion object {
    fun newGame(size: Int): GameState {
      return GameState(
              board = GoBoard.emptyBoard(size),
      playerJustMoved = Player.White,
      prevPrevMove = Location.undefined,
      prevMove = Location.undefined,
      blackStonesTaken = 0,
      whiteStonesTaken = 0)
    }
  }
}

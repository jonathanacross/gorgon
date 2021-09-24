package gorgon.client

import gorgon.engine.EngineFactory
import gorgon.gobase.Game
import gorgon.gobase.GoBoard
import gorgon.gobase.Location
import gorgon.gobase.Player
import kotlin.system.exitProcess

// Go client for the Go Text Protocol
// See http://www.lysator.liu.se/~gunnar/gtp/gtp2-spec-draft2/gtp2-spec.html
class GtpClient(private val engineParams: List<String>) {
    private var size = 19
    private var komi = 0.0
    private var game = Game(size, komi)
    private val engine = EngineFactory.newEngine(engineParams)
    private val commands = listOf(
        "name",
        "version",
        "protocol_version",
        "known_command",
        "list_commands",
        "clear_board",
        "boardsize",
        "komi",
        "undo",
        "genmove",
        "play",
        "quit",
        "final_score",
        // analysis commands
        "gogui-analyze_commands",
        "showboard"
    )

    fun processCommand(input: String) {
        val tokens = input.split("\\s+".toRegex())
        val commandStr = tokens[0]

        val response =
            when (commandStr) {
                "name" -> doName()
                "version" -> doVersion()
                "protocol_version" -> doProtocolVersion()
                "known_command" -> doKnownCommand(tokens)
                "list_commands" -> doListCommands()
                "clear_board" -> doClearBoard()
                "boardsize" -> doBoardSize(tokens)
                "komi" -> doSetKomi(tokens)
                "undo" -> doUndo()
                "genmove" -> doGenMove(tokens)
                "play" -> doPlay(tokens)
                "quit" -> doQuit()
                "final_score" -> doFinalScore()
                "gogui-analyze_commands" -> doAnalyzeCommands()
                "showboard" -> doShowBoard()
                else -> doUnknown()
            }

        print("= " + response + "\n\n")

        if (commandStr == "quit") {
            exitProcess(0)
        }
    }

    private fun doName() = "gorgon" + engineParams.joinToString(" ")

    private fun doVersion() = "0.0.0"

    private fun doProtocolVersion() = "2"

    private fun doKnownCommand(args: List<String>): String {
        return if (commands.contains(args[1])) "true" else "false"
    }

    private fun doListCommands() = commands.joinToString("\n")

    private fun doClearBoard(): String {
        game = Game(size, komi)
        return ""
    }

    private fun doBoardSize(args: List<String>): String {
        val attemptSize = args[1].toInt()
        return if (GoBoard.minSize <= attemptSize && attemptSize <= GoBoard.maxSize) {
            size = attemptSize
            game = Game(size, komi)
            ""
        } else {
            "unacceptable size"
        }
    }

    private fun doUndo(): String {
        game.undoMove()
        return ""
    }

    private fun doGenMove(args: List<String>): String {
        val player = Player.parsePlayerString(args[1])
        val location = engine.suggestMove(player, game.currState(), game.komi)
        game.playMove(player, location)
        return Location.idxToString(location)
    }

    private fun doSetKomi(args: List<String>): String {
        komi = args[1].toDouble()
        game.komi = komi
        return ""
    }

    private fun doQuit() = ""

    private fun doPlay(args: List<String>): String {
        val p = Player.parsePlayerString(args[1])
        val loc = Location.stringToIdx(args[2])
        game.playMove(p, loc)
        return ""
    }

    private fun doFinalScore(): String {
        return "0"
    }

    private fun doUnknown() = "unknown command"

    // ----------- analysis commands (standard commands that gogui handles)
    private fun doAnalyzeCommands(): String {
        return listOf("string/ShowBoard/showboard").joinToString("\n")
    }

    private fun doShowBoard() = game.currBoard().toString()
}

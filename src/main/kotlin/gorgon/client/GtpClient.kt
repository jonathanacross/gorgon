package gorgon.client

import gorgon.gobase.Game
import gorgon.gobase.Location
import gorgon.gobase.Player
import kotlin.system.exitProcess

fun interface GtpCommand {
    fun apply(args: List<String>): String
}

// Go Client for the Go Text Protocol
// See http://www.lysator.liu.se/~gunnar/gtp/gtp2-spec-draft2/gtp2-spec.html
class GtpClient {

    fun processCommand(input: String) {
        val tokens = input.split("\\s+".toRegex())
        val commandStr = tokens[0]

        val command = commands.getOrDefault(commandStr, DoUnknown)
        val result = command.apply(tokens)
        print("= " + result + "\n\n")

        if (commandStr == "quit") {
            exitProcess(0)
        }
    }

    companion object {
        var size = 19
        var komi = 0.0
        var game = Game(size, komi)

        val commands: Map<String, GtpCommand> = mapOf(
            // core commands
            "name" to DoName,
            "version" to DoVersion,
            "protocol_version" to DoProtocolVersion,
            "list_commands" to DoListCommands,
            "clear_board" to DoClearBoard,
            "boardsize" to DoBoardSize,
            "komi" to DoSetKomi,
            "undo" to DoUndo,
            "genmove" to DoGenMove,
            "play" to DoPlay,
            "quit" to DoQuit,
            "final_score" to DoFinalScore,
            // analysis commands
            "gogui-analyze_commands" to DoAnalyzeCommands,
            "showboard" to DoShowBoard,
        )

        object DoName : GtpCommand {
            override fun apply(args: List<String>): String {
                return "gorgon"
            }
        }

        object DoVersion : GtpCommand {
            override fun apply(args: List<String>): String {
                return "0.0.0"
            }
        }

        object DoProtocolVersion : GtpCommand {
            override fun apply(args: List<String>): String {
                return "2"
            }
        }

        object DoListCommands : GtpCommand {
            override fun apply(args: List<String>): String {
                return commands.keys.joinToString("\n")
            }
        }

        object DoClearBoard : GtpCommand {
            override fun apply(args: List<String>): String {
                game = Game(size, komi)
                return ""
            }
        }

        object DoBoardSize : GtpCommand {
            override fun apply(args: List<String>): String {
                val attemptSize = args[1].toInt()
                return if (1 <= attemptSize && attemptSize <= 19) {
                    size = attemptSize
                    game = Game(size, komi)
                    ""
                } else {
                    "unacceptable size"
                }
            }
        }

        object DoUndo : GtpCommand {
            override fun apply(args: List<String>): String {
                game.undoMove()
                return ""
            }
        }

        object DoGenMove : GtpCommand {
            override fun apply(args: List<String>): String {
                return "pass"
            }
        }

        object DoSetKomi : GtpCommand {
            override fun apply(args: List<String>): String {
                komi = args[1].toDouble()
                game.komi = komi
                return ""
            }
        }

        object DoQuit : GtpCommand {
            override fun apply(args: List<String>): String {
                return ""
            }
        }

        object DoPlay : GtpCommand {
            override fun apply(args: List<String>): String {
                val p = Player.parsePlayerString(args[1])
                val loc = Location.stringToIdx(args[2])
                game.playMove(p, loc)
                return ""
            }
        }

        object DoFinalScore : GtpCommand {
            override fun apply(args: List<String>): String {
                return "0"
            }
        }

        object DoUnknown : GtpCommand {
            override fun apply(args: List<String>): String {
                return "unknown command"
            }
        }

        // ----------- analysis commands (standard commands that gogui handles)
        object DoAnalyzeCommands: GtpCommand {
            override fun apply(args: List<String>): String  {
                return listOf( "string/ShowBoard/showboard").joinToString("\n")
            }
        }

        object DoShowBoard : GtpCommand {
            override fun apply(args: List<String>) : String{
                return game.currBoard().toString()
            }
        }

    }
}

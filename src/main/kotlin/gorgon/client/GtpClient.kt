package gorgon.client

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

        val commands: Map<String, GtpCommand> = mapOf(
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
            "final_score" to DoFinalScore
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
                return ""
            }
        }

        object DoBoardSize : GtpCommand {
            override fun apply(args: List<String>): String {
                val attemptSize = args[1].toInt()
                return if (1 <= attemptSize && attemptSize <= 19) {
                    size = attemptSize
                    ""
                } else {
                    "unacceptable size"
                }
            }
        }

        object DoUndo : GtpCommand {
            override fun apply(args: List<String>): String {
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
                return "pass"
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
    }
}

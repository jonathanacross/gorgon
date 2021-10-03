package gorgon.client

import gorgon.engine.EngineFactory
import gorgon.engine.FeatureExtractor
import gorgon.engine.Utils
import gorgon.gobase.*
import kotlin.system.exitProcess

sealed class Response {
    abstract fun success(): Boolean
    abstract fun text(): String

    data class Success(val text: String) : Response() {
        override fun success() = true
        override fun text() = text
    }

    data class Failure(val text: String) : Response() {
        override fun success() = false
        override fun text() = text
    }
}

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
        "showboard",
        "all_legal",
        "gorgon-probs",
        "gorgon-feature_values"
    )

    fun processCommand(input: String) {
        val tokens = input.split("\\s+".toRegex()).toMutableList()
        // Commands may start with an optional integer id.
        // If this happens, save it and strip it off.
        val commandId = tokens[0].toIntOrNull()
        if (commandId != null) {
            tokens.removeFirst()
        }
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
                "all_legal" -> doAllLegal(tokens)
                "gorgon-probs" -> doMoveProbs(tokens)
                "gorgon-feature_values" -> doFeatureValues(tokens)
                else -> doUnknown()
            }

        val prefix = if (response.success()) "=" else "?"
        val idPart = commandId ?: ""
        print(prefix + idPart + " " + response.text() + "\n\n")

        if (commandStr == "quit") {
            exitProcess(0)
        }
    }

    private fun doName() = Response.Success("gorgon " + engineParams.joinToString(" "))

    private fun doVersion() = Response.Success("0.0.0")

    private fun doProtocolVersion() = Response.Success("2")

    private fun doKnownCommand(args: List<String>): Response {
        return if (commands.contains(args[1])) Response.Success("true") else Response.Success("false")
    }

    private fun doListCommands() = Response.Success(commands.joinToString("\n"))

    private fun doClearBoard(): Response {
        game = Game(size, komi)
        return Response.Success("")
    }

    private fun doBoardSize(args: List<String>): Response {
        val attemptSize =
            args[1].toIntOrNull() ?: return Response.Failure("couldn't parse board size")
        return if (GoBoard.minSize <= attemptSize && attemptSize <= GoBoard.maxSize) {
            size = attemptSize
            game = Game(size, komi)
            Response.Success("")
        } else {
            Response.Failure("unacceptable size")
        }
    }

    private fun doUndo(): Response {
        game.undoMove()
        return Response.Success("")
    }

    private fun doGenMove(args: List<String>): Response {
        val player = Player.parsePlayerString(args[1])
        val location = engine.suggestMove(player, game.currState(), game.komi)
        game.playMove(player, location)
        return Response.Success(Location.idxToString(location))
    }

    private fun doSetKomi(args: List<String>): Response {
        val parsedKomi = args[1].toDoubleOrNull()
        return if (parsedKomi == null) {
            Response.Failure("Couldn't parse komi " + args[1])
        } else {
            game.komi = parsedKomi
            Response.Success("")
        }
    }

    private fun doQuit() = Response.Success("")

    private fun doPlay(args: List<String>): Response {
        if (args.size < 3) {
            return Response.Failure("expected play <player> <location>")
        }
        return try {
            val p = Player.parsePlayerString(args[1])
            val loc = Location.stringToIdx(args[2])
            if (loc == Location.pass) {
                return Response.Success("")
            }
            if (!game.currBoard().isLegalMove(SquareType.playerToSquareType(p), loc)) {
                return Response.Failure("that's not a legal move")
            }
            game.playMove(p, loc)
            Response.Success("")
        } catch (e: Exception) {
            Response.Failure("error parsing player or location")
        }
    }

    private fun doFinalScore(): Response {
        val (b, w) = game.currBoard().score()
        val score = b - (w + game.komi)
        val textScore =
            if (score > 0) "B+" + (score).toString()
            else if (score < 0) "W+" + (-score).toString()
            else "0"

        return Response.Success(textScore)
    }

    private fun doUnknown() = Response.Failure("unknown command")

    // ----------- analysis commands (standard commands that gogui handles)
    private fun doAnalyzeCommands(): Response {
        return Response.Success(
            listOf(
                "string/ShowBoard/showboard",
                "plist/All Legal/all_legal %c",
                "dboard/Probs/gorgon-probs %c",
                "dboard/Feature Values/gorgon-feature_values %c %s",
            ).joinToString("\n")
        )
    }

    private fun doShowBoard() = Response.Success(game.currBoard().toString())

    private fun doAllLegal(args: List<String>): Response {
        val player = Player.parsePlayerString(args[1])
        val legalMoves = game.currState().legalMoves(player)
        val legalMovesString = legalMoves
            .map { loc -> Location.idxToString(loc) }
            .joinToString(" ")

        return Response.Success(legalMovesString)
    }

    private fun pointsToBoard(vals: List<Pair<Int, Double>>): Array<DoubleArray> {
        val board = Array(size, { DoubleArray(size) })
        for (i in 0 until size) {
            for (j in 0 until size) {
                board[i][j] = 0.0
                for (v in vals) {
                    val (r, c) = Location.idxToRowCol(v.first)
                    board[r - 1][c - 1] = v.second
                }
            }
        }

        return board
    }

    private fun doMoveProbs(args: List<String>): Response {
        val player = Player.parsePlayerString(args[1])
        val probs = engine.moveProbs(player, game.currState(), game.komi)
        val board = pointsToBoard(probs)
        val sb = StringBuilder()
        for (r in size - 1 downTo 0) {
            for (c in 0 until size) {
                sb.append(board[r][c].toString() + " ")
            }
            sb.append("\n")
        }
        return Response.Success(sb.toString())
    }

    private fun computeFeatureValues(player: Player, feature: String): List<Pair<Int, Double>> {
        val nonBadMoves = Utils.getNonBadMoves(player, game.currState())
        if (nonBadMoves.size == 1 && nonBadMoves[0] == Location.pass) {
            return listOf()
        }

        val extractor = FeatureExtractor(game.currState().board, player)
        val locScores = nonBadMoves.map { loc ->
            Pair(loc, extractor.getFeature(feature, loc).toDouble())
            //Pair(loc, Utils.sigmoid(extractor.getFeature(feature, loc).toDouble()))
        }
        val scores = locScores.map { x -> x.second }
        val minScore = scores.minOrNull()
        val maxScore = scores.maxOrNull()
        if (minScore == maxScore) {
            return locScores.map { x -> Pair(x.first, 1.0) }
        }
        if (minScore != null && maxScore != null) {
            val scale = 1.0 / (maxScore - minScore)
            return locScores.map { x -> Pair(x.first, (x.second - minScore) * scale) }
        }
        return listOf()
    }

    private fun doFeatureValues(args: List<String>): Response {
        val player = Player.parsePlayerString(args[1])
        val feature = args[2]
        val values = computeFeatureValues(player, feature)

        val board = pointsToBoard(values)
        val sb = StringBuilder()
        for (r in size - 1 downTo 0) {
            for (c in 0 until size) {
                sb.append(board[r][c].toString() + " ")
            }
            sb.append("\n")
        }
        return Response.Success(sb.toString())
    }
}

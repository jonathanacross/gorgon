package gorgon.client

import gorgon.engine.EngineFactory
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("usage: java -jar jarfile.jar <engine params>")
        println("    engine can be one of " + EngineFactory.engines.keys.joinToString(", "))
        exitProcess(0)
    }
    val client = GtpClient(args.toList())

    for (command in generateSequence(::readLine)) {
        client.processCommand(command)
    }
}
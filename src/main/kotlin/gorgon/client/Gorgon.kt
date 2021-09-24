package gorgon.client

fun main(args: Array<String>) {
    val client = GtpClient(listOf("random"))

    for (command in generateSequence(::readLine)) {
        client.processCommand(command)
    }
}
package gorgon.client

fun main(args: Array<String>) {
    val client = GtpClient()

    for (command in generateSequence(::readLine)) {
        client.processCommand(command)
    }
}
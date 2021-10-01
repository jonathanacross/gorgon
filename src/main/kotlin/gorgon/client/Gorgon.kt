package gorgon.client

fun main(args: Array<String>) {
    val client = GtpClient(listOf("noeye"))

    for (command in generateSequence(::readLine)) {
        client.processCommand(command)
    }
}
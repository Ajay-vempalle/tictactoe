package com.com.ajayvempalle.tictactoe

import io.ktor.server.application.*
import io.ktor.server.response.respondText
import io.ktor.server.routing.*

fun Application.configureRouting() {
    val game = TicTacToe()
    routing {

        get("/") {
            call.respondText("Backend Working")
        }

        socket(game)
    }
}
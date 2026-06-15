package com.com.ajayvempalle.tictactoe

import com.com.ajayvempalle.tictactoe.model.MakeTurn
import io.ktor.server.routing.Route
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.json.Json

fun Route.socket(game: TicTacToe) {

    webSocket("/play") {

        println("CLIENT CONNECTED")

        val player = game.connectPlayer(this)

        if (player == null) {
            close()
            return@webSocket
        }

        try {
            incoming.consumeEach { frame ->

                if (frame is Frame.Text) {

                    val turn = Json.decodeFromString<MakeTurn>(
                        frame.readText()
                    )

                    game.finishTurn(
                        player = player,
                        x = turn.x,
                        y = turn.y
                    )
                }
            }
        } finally {
            game.disconnectPlayer(player)
        }
    }
}
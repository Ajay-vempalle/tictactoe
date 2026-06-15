package com.com.ajayvempalle.tictactoe

import com.com.ajayvempalle.tictactoe.model.GameState
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.send
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap

class TicTacToe {
    private var playerSockets = ConcurrentHashMap<Char, WebSocketSession>()
    private val state = MutableStateFlow(GameState())
    private val gameScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var delayGame: Job? = null

    init {
        state.onEach(::broadCast).launchIn(gameScope)
    }

    fun connectPlayer(session: WebSocketSession): Char? {
        val isPlayerX = state.value.connectedPlayers.any { it == 'X' }
        val player = if (isPlayerX) 'O' else 'X'
        state.update {

            if (state.value.connectedPlayers.contains(player)) {
                return null
            }
            if (!playerSockets.containsKey(player)) {
                playerSockets[player] = session
            }

            it.copy(
                gamerTurn = player,
                connectedPlayers = it.connectedPlayers + player
            )
        }
        return player
    }

    fun disconnectPlayer(player: Char) {
        playerSockets.remove(player)
        state.update {
            it.copy(
                connectedPlayers = it.connectedPlayers - player
            )
        }
    }

    suspend fun broadCast(state: GameState) {
        playerSockets.values.forEach { session ->
            session.send(
                Json.encodeToString(state)
            )
        }
    }

    fun finishTurn(player: Char, x: Int, y: Int) {
        if (state.value.field[y][x] != null || state.value.winningPlayer != null) {
            return
        }
        if (state.value.gamerTurn != player) {
            return
        }
        state.update {
            val newField = it.field.also { field ->
                field[y][x] = player
            }
            val isBoardFull = newField.all {
                it.all {
                    it != null
                }
            }
            if (isBoardFull) {
                delayAndStartFreshGame()
            }

            it.copy(
                gamerTurn = if (player == 'X') 'O' else 'X',
                field = newField,
                isBoardFull = isBoardFull,
                winningPlayer = getWinningPlayer(newField)?.also {
                    delayAndStartFreshGame()
                }
            )
        }
    }

    fun getWinningPlayer(field: Array<Array<Char?>>): Char? {
        return when {
            // Rows
            field[0][0] != null &&
                    field[0][0] == field[0][1] &&
                    field[0][1] == field[0][2] -> field[0][0]

            field[1][0] != null &&
                    field[1][0] == field[1][1] &&
                    field[1][1] == field[1][2] -> field[1][0]

            field[2][0] != null &&
                    field[2][0] == field[2][1] &&
                    field[2][1] == field[2][2] -> field[2][0]

            // Columns
            field[0][0] != null &&
                    field[0][0] == field[1][0] &&
                    field[1][0] == field[2][0] -> field[0][0]

            field[0][1] != null &&
                    field[0][1] == field[1][1] &&
                    field[1][1] == field[2][1] -> field[0][1]

            field[0][2] != null &&
                    field[0][2] == field[1][2] &&
                    field[1][2] == field[2][2] -> field[0][2]

            // Diagonals
            field[0][0] != null &&
                    field[0][0] == field[1][1] &&
                    field[1][1] == field[2][2] -> field[0][0]

            field[0][2] != null &&
                    field[0][2] == field[1][1] &&
                    field[1][1] == field[2][0] -> field[0][2]

            else -> null
        }
    }

    private fun delayAndStartFreshGame() {
        delayGame?.cancel()
        delayGame = gameScope.launch {
            delay(5000L)
            state.update {
                it.copy(
                    gamerTurn = 'X',
                    field = GameState.Companion.getEmptyField(),
                    winningPlayer = null,
                    isBoardFull = false
                )
            }

        }
    }

}
package com.com.ajayvempalle.tictactoe.model

import kotlinx.serialization.Serializable

@Serializable
data class GameState(
    val gamerTurn: Char? = 'X',
    var field: Array<Array<Char?>> = getEmptyField(),
    var isBoardFull: Boolean = false,
    var winningPlayer: Char? = null,
    var connectedPlayers: List<Char> = emptyList()
) {
    companion object {
        fun getEmptyField(): Array<Array<Char?>> {
            return arrayOf(
                arrayOf(null, null, null),
                arrayOf(null, null, null),
                arrayOf(null, null, null)
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GameState

        if (gamerTurn != other.gamerTurn) return false
        if (isBoardFull != other.isBoardFull) return false
        if (winningPlayer != other.winningPlayer) return false
        if (!field.contentDeepEquals(other.field)) return false
        if (connectedPlayers != other.connectedPlayers) return false

        return true
    }

    override fun hashCode(): Int {
        var result = gamerTurn?.hashCode() ?: 0
        result = 31 * result + isBoardFull.hashCode()
        result = 31 * result + (winningPlayer?.hashCode() ?: 0)
        result = 31 * result + field.contentDeepHashCode()
        result = 31 * result + connectedPlayers.hashCode()
        return result
    }
}
package com.example.mavericksapp

import com.airbnb.mvrx.MavericksState

data class GameState(
    val data: List<CellState> = emptyState,
    val isXTurn: Boolean = true,
    val score: Pair<Int, Int> = Pair(0, 0)
) :
    MavericksState {
    private val winLines = arrayOf(
        listOf(0, 1, 2),
        listOf(0, 3, 6),
        listOf(0, 4, 8),
        listOf(1, 4, 7),
        listOf(2, 4, 6),
        listOf(2, 5, 8),
        listOf(3, 4, 5),
        listOf(6, 7, 8),
    )

    private fun controlALine(line: List<Int>): Boolean {
        val first = data[line[0]]
        return line.all {
            when (data[it]) {
                is CellState.Empty -> false
                is CellState.O -> first is CellState.O
                is CellState.X -> first is CellState.X
            }
        }
    }

    private fun controlAllLines(): TurnState {
        for (line in winLines) {
            if (controlALine(line))
                return TurnState.Won(data[line[0]], line)
        }
        return if (data.contains(CellState.Empty))
            TurnState.IsRunning
        else
            TurnState.Draw
    }

    sealed class TurnState {
        object IsRunning : TurnState()
        object Draw : TurnState()
        data class Won(val winner: CellState, val line: List<Int>) : TurnState()
    }

    val turnState: TurnState = controlAllLines()

    /*
        0 1 2
        3 4 5
        6 7 8
    */
    fun get(row: Int, column: Int): CellState {
        return data[row * 3 + column]
    }


}

sealed class CellState {
    object X : CellState()
    object O : CellState()
    object Empty : CellState()
}
package com.example.mavericksapp

import com.airbnb.mvrx.MavericksViewModel


val emptyState = List(9) { CellState.Empty }


class GridViewmodel(initialState: GameState) : MavericksViewModel<GameState>(initialState) {

    init {
        onEach(GameState::turnState) { turnState ->
            when (turnState) {
                GameState.TurnState.IsRunning -> Unit
                is GameState.TurnState.Won -> increaseScore()
                GameState.TurnState.Draw -> Unit
            }
        }
/*
        onEach() { state ->
            if (!state.isXTurn && state.turnState is GameState.TurnState.IsRunning) {
                /*
                    0 1 2
                    3 4 5
                    6 7 8
                */
                for (index in state.data.indices) {
                    if (state.data[index] is CellState.Empty) {
                        val row = index.floorDiv(3)
                        val column = index - 3 * row
                        update(row, column)
                        break
                    }
                }
            }

        }
*/

    }

    private fun increaseScore() {
        withState {
            if (it.turnState is GameState.TurnState.Won)
                when (it.turnState.winner) {
                    is CellState.X -> setState { copy(score = Pair(score.first + 1, score.second)) }
                    is CellState.O -> setState { copy(score = Pair(score.first, score.second + 1)) }
                    CellState.Empty -> TODO()
                }
        }
    }

    fun update(row: Int, column: Int) = withState {
        if (it.turnState !is GameState.TurnState.IsRunning)
            setState { copy(data = emptyState) }

        if (it.get(row, column) !is CellState.Empty) {
            return@withState
        }

        setState {
            val newData = data.mapIndexed { index, s ->
                if (index == row * 3 + column) {
                    return@mapIndexed if (isXTurn)
                        CellState.X
                    else
                        CellState.O
                }
                s
            }
            copy(data = newData, isXTurn = !isXTurn)
        }
    }

    fun reset() {
        setState { copy(data = emptyState, score = Pair(0, 0)) }
    }
}
package com.example.mavericksapp

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.Animatable
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.airbnb.mvrx.compose.collectAsState
import com.airbnb.mvrx.compose.mavericksViewModel
import com.example.mavericksapp.ui.theme.MavericksAppTheme
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Mavericks.initialize(this)
        setContent {
            MavericksAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    GameScreen()
                }
            }
        }
    }
}


@Composable
fun GameScreen() {
    var orientation by remember {
        mutableStateOf(Configuration.ORIENTATION_UNDEFINED)
    }

    val configuration = LocalConfiguration.current

    LaunchedEffect(key1 = configuration) {
        snapshotFlow { configuration.orientation }
            .collect { orientation = it }
    }

    val viewModel: GridViewmodel = mavericksViewModel()
    val state by viewModel.collectAsState()

    when (orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> LandscapeGame(
            orientation,
            state.score
        ) { viewModel.reset() }
        Configuration.ORIENTATION_PORTRAIT -> PortraitGame(
            orientation,
            state.score
        ) { viewModel.reset() }
    }

}

@Composable
fun LandscapeGame(orientation: Int, score: Pair<Int, Int>, reset: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        Column(
            Modifier
                .fillMaxHeight()
                .weight(1f)
                .align(Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = "X - " + score.first, style = MaterialTheme.typography.h5
            )
            Text(text = "O - " + score.second, style = MaterialTheme.typography.h5)
        }
        Spacer(modifier = Modifier.height(8.dp))

        Box(modifier = Modifier.weight(1f)) {
            GameCanvas(orientation)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { reset() },
            modifier = Modifier.weight(10f, fill = false)
        ) {
            Text(text = "Reset")
        }

    }
}

@Composable
fun PortraitGame(orientation: Int, score: Pair<Int, Int>, reset: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row(
            Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
                .weight(2f),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "X - " + score.first, style = MaterialTheme.typography.h4)
            Text(text = "O - " + score.second, style = MaterialTheme.typography.h4)
        }
        Spacer(modifier = Modifier.height(1.dp))
        Box(modifier = Modifier.weight(29f)) {
            GameCanvas(orientation)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { reset() },
            modifier = Modifier.weight(8f, fill = false)
        ) {
            Text(text = "Reset")
        }

    }
}

@Composable
fun GameCanvas(orientation: Int) {
    var offset by remember {
        mutableStateOf(Pair(0, 0))
    }

    val viewModel: GridViewmodel = mavericksViewModel()
    val state by viewModel.collectAsState()

    val animColor = remember { Animatable(Color.Black) }

    LaunchedEffect(key1 = state.turnState) {
        when (state.turnState) {
            is GameState.TurnState.Won ->
                animColor.animateTo(Color.Red, tween(500,easing = LinearEasing))
            is GameState.TurnState.IsRunning -> animColor.snapTo(Color.Black)
            else -> Unit
        }
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .aspectRatio(
                1f,
                matchHeightConstraintsFirst = Configuration.ORIENTATION_LANDSCAPE == orientation
            )
            .pointerInput(Unit) {
                coroutineScope {
                    while (true) {
                        val down = awaitPointerEventScope {
                            awaitFirstDown()
                        }

                        offset = Pair(
                            down.position.y
                                .div(size.height / 3.00f)
                                .toInt(),
                            down.position.x
                                .div(size.width / 3.00f)
                                .toInt()
                        )
                        viewModel.update(offset.first, offset.second)
                    }
                }
            },
    ) {
        val gridSignMap = mapOf(
            0 to Offset(size.width / 6f, size.height / 6f),
            1 to Offset(3 * size.width / 6f, size.height / 6f),
            2 to Offset(5 * size.width / 6f, size.height / 6f),
            3 to Offset(size.width / 6f, size.height / 2f),
            4 to Offset(3 * size.width / 6f, size.height / 2f),
            5 to Offset(5 * size.width / 6f, size.height / 2f),
            6 to Offset(size.width / 6f, 5 * size.height / 6f),
            7 to Offset(3 * size.width / 6f, 5 * size.height / 6f),
            8 to Offset(5 * size.width / 6f, 5 * size.height / 6f),
        )

        state.data.forEachIndexed { index, cell ->
            var color: Color = Color.Black
            when (state.turnState) {
                is GameState.TurnState.Won -> if ((state.turnState as GameState.TurnState.Won).line.contains(
                        index
                    )
                ) color = animColor.value
                else -> Unit
            }
            when (cell) {
                is CellState.Empty -> Unit
                CellState.O -> drawCircle(
                    color = color,
                    radius = 40.dp.toPx(),
                    center = gridSignMap[index]!!,
                    style = Stroke(width = 15.dp.toPx())
                )
                CellState.X -> {
                    drawLine(
                        strokeWidth = 15.dp.toPx(),
                        color = color,
                        start = gridSignMap[index]!!.minus(Offset(40.dp.toPx(), 40.dp.toPx())),
                        end = gridSignMap[index]!!.plus(Offset(40.dp.toPx(), 40.dp.toPx()))
                    )
                    drawLine(
                        strokeWidth = 15.dp.toPx(),
                        color = color,
                        start = gridSignMap[index]!!.plus(Offset(40.dp.toPx(), -40.dp.toPx())),
                        end = gridSignMap[index]!!.minus(Offset(40.dp.toPx(), -40.dp.toPx()))
                    )
                }
            }
        }


        drawLine(
            strokeWidth = 3f,
            color = Color.Black,
            start = Offset(
                x = 0f,
                y = size.height / 3f
            ),
            end = Offset(x = size.width, y = size.height / 3f)
        )

        drawLine(
            strokeWidth = 3f,
            color = Color.Black,
            start = Offset(
                x = 0f,
                y = 2 * size.height / 3f
            ),
            end = Offset(x = size.width, y = 2 * size.height / 3f)
        )

        drawLine(
            strokeWidth = 3f,
            color = Color.Black,
            start = Offset(
                x = size.width / 3f,
                y = 0f
            ),
            end = Offset(x = size.width / 3f, y = size.height)
        )

        drawLine(
            strokeWidth = 3f,
            color = Color.Black,
            start = Offset(
                x = 2 * size.width / 3f,
                y = 0f
            ),
            end = Offset(x = 2 * size.width / 3f, y = size.height)
        )

    }
}

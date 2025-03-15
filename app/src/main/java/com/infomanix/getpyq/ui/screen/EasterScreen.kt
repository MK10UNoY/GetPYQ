package com.infomanix.getpyq.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.random.Random

@Composable
fun EasterScreen(navController: NavController) {
    var score by remember { mutableStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Score: $score",
                fontSize = 24.sp,
                color = Color.White,
                modifier = Modifier.padding(36.dp)
            )

            if (gameOver) {
                Text(
                    text = "Game Over!",
                    fontSize = 32.sp,
                    color = Color.Red,
                    modifier = Modifier.padding(16.dp)
                )
                Button(
                    onClick = {
                        score = 0
                        gameOver = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Text("Restart", color = Color.Black)
                }
            } else {
                HeartGame(score) { newScore, isGameOver ->
                    score = newScore
                    gameOver = isGameOver
                }
            }
        }
    }
}

@Composable
fun HeartGame(score: Int, onGameUpdate: (Int, Boolean) -> Unit) {
    var heartPosition by remember { mutableStateOf(Offset(100f, 500f)) }
    var thornPosition by remember { mutableStateOf(Offset(800f, 500f)) }
    var isBroken by remember { mutableStateOf(false) }
    var speed by remember { mutableStateOf(8f) } // Initial speed of thorns
    val coroutineScope = rememberCoroutineScope()

    // Game loop to move the thorn and increase score
    LaunchedEffect(Unit) {
        while (true) {
            delay(16L) // Frame delay for smooth movement
            thornPosition = thornPosition.copy(x = thornPosition.x - speed)

            // Increase speed gradually for difficulty
            speed += 0.002f

            // Reset thorn when it goes off-screen
            if (thornPosition.x < -50f) {
                thornPosition = thornPosition.copy(x = 800f) // Respawn at right
            }

            // Increase score as player survives
            onGameUpdate(score + 1, false)

            // Collision detection
            if (abs(heartPosition.x - thornPosition.x) < 50 &&
                abs(heartPosition.y - thornPosition.y) < 50
            ) {
                isBroken = true
                onGameUpdate(score, true) // Game Over
            }
        }
    }

    // Smooth Gravity Logic
    LaunchedEffect(Unit) {
        while (true) {
            delay(30) // Reduce gravity effect
            if (heartPosition.y < 500f) { // Simulate gravity
                heartPosition = heartPosition.copy(y = heartPosition.y + 10f)
            }
        }
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures {
                    coroutineScope.launch {
                        heartPosition = heartPosition.copy(y = heartPosition.y - 100f)
                    }
                }
            }
    ) {
        if (isBroken) {
            drawBrokenHeart(heartPosition, Color.Red) // Draw broken heart
        } else {
            drawHeart(heartPosition, Color.Red) // Normal heart
        }
        drawThorn(thornPosition, Color.Green)
    }
}

// Draw normal heart
fun DrawScope.drawHeart(center: Offset, color: Color) {
    drawCircle(color, radius = 50f, center = center)
}

// Draw broken heart
fun DrawScope.drawBrokenHeart(center: Offset, color: Color) {
    drawArc(
        color, startAngle = -30f, sweepAngle = 180f, useCenter = true,
        topLeft = center - Offset(50f, 50f), size = Size(50f, 50f)
    )
    drawArc(
        color, startAngle = 30f, sweepAngle = 180f, useCenter = true,
        topLeft = center - Offset(0f, 50f), size = Size(50f, 50f)
    )
}

// Draw thorns
fun DrawScope.drawThorn(position: Offset, color: Color) {
    drawRect(color, size = Size(50f, 100f), topLeft = position)
}

// Draw starry background
fun DrawScope.drawStarryBackground() {
    repeat(100) {
        val x = Random.nextFloat() * size.width
        val y = Random.nextFloat() * size.height
        drawCircle(Color.White, radius = 2f, center = Offset(x, y))
    }
}
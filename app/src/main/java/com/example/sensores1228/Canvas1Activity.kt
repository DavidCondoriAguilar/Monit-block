package com.example.sensores1228

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size


import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.sensores1228.ui.theme.Sensores1228Theme

class Canvas1Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            dibujarCanvas()
        }
    }
}

@Composable
fun dibujarCanvas(){
    Canvas (Modifier.fillMaxSize()){
        drawLine(
            color = Color.Red,
            strokeWidth = 10f,
            start = Offset(0f, 0f),
            end = Offset(500f, 1000f)
        )
        drawLine(
            color = Color.Blue,
            strokeWidth = 30f,
            start = Offset(300f, 100f),
            end = Offset(0f, 1500f)
        )
        drawCircle(
            color = Color.Green,
            radius = 100f,
            center = Offset(700f, 700f)
        )
        drawArc(
            color = Color.Yellow,
            startAngle = 0f,
            sweepAngle = 270f,
            useCenter = true,
            topLeft = Offset(500f, 1000f),
            size = Size(500f, 500f)

        )
    }
}
package com.example.sensores1228

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.sensores1228.ui.theme.Sensores1228Theme
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput

class Canvas2Activity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DibujarCanvas2()
        }
    }
}
@Composable
fun DibujarCanvas2(){
    var center by remember { mutableStateOf(Offset.Zero)}
    Canvas(Modifier.fillMaxSize().pointerInput(Unit){
        detectTapGestures(
            onTap = {
                center = it
            }
        )
    }){
        drawCircle(
            color = androidx.compose.ui.graphics.Color.Red,
            radius = 100f,
            center = center
        )
    }
}
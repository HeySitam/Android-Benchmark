package dev.heysitam.macrobenchmarktest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dev.heysitam.macrobenchmarktest.ui.theme.MacrobenchmarkTestTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MacrobenchmarkTestTheme {
                TodoScreen()
            }
        }
    }
}
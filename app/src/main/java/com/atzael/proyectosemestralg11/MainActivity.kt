package com.atzael.proyectosemestralg11

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.atzael.proyectosemestralg11.navigation.AppNavigation
import com.atzael.proyectosemestralg11.ui.theme.ProyectoSemestralG11Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProyectoSemestralG11Theme {
                AppNavigation()
            }
        }
    }
}

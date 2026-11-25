package com.atzael.proyectosemestralg11

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.atzael.proyectosemestralg11.data.remote.SyncManager
import com.atzael.proyectosemestralg11.navigation.AppNavigation
import com.atzael.proyectosemestralg11.ui.theme.ProyectoSemestralG11Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Iniciar el gestor de sincronización automática (sube datos pendientes a Supabase)
        SyncManager.iniciarSincronizacionAuto(this)

        setContent {
            ProyectoSemestralG11Theme {
                AppNavigation()
            }
        }
    }
}

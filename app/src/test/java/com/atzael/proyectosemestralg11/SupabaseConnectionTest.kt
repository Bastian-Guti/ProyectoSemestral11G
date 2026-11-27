package com.atzael.proyectosemestralg11

import com.atzael.proyectosemestralg11.data.remote.SupabaseClient
import kotlinx.coroutines.runBlocking
import org.junit.Test

class SupabaseConnectionTest {

    @Test
    fun testConexionSupabase() = runBlocking {
        println("=== PROBANDO CONEXIÓN A SUPABASE ===")
        try {
            val fotos = SupabaseClient.api.getFotosOrden()
            println("TOTAL DE FOTOS EN SUPABASE: ${fotos.size}")
            fotos.forEach { f ->
                println("  • ID: ${f.id} | Tipo: ${f.tipo} | Orden: ${f.ordenId}")
            }
        } catch (e: Exception) {
            println(" ERROR: ${e.message}")
            throw e
        }
    }
}

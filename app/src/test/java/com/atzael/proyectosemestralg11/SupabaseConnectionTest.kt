package com.atzael.proyectosemestralg11

import com.atzael.proyectosemestralg11.data.remote.SupabaseClient
import kotlinx.coroutines.runBlocking
import org.junit.Test

class SupabaseConnectionTest {

    @Test
    fun testConexionSupabase() = runBlocking {
        println("=== PROBANDO CONEXIÓN A SUPABASE CON RETROFIT ===")
        try {
            val usuarios = SupabaseClient.api.getUsuarios()
            println("✅ CONEXIÓN Y AUTENTICACIÓN EXITOSA!")
            println("Usuarios encontrados en Supabase: ${usuarios.size}")
            usuarios.forEach { u ->
                println("  • RUT: ${u.rut} | Nombre: ${u.nombre} ${u.apellido} | Rol: ${u.rol}")
            }

            val clientes = SupabaseClient.api.getClientes()
            println("Clientes encontrados en Supabase: ${clientes.size}")
            clientes.forEach { c ->
                println("  • ID: ${c.id} | Nombre: ${c.nombre} | Contacto: ${c.contacto}")
            }
        } catch (e: Exception) {
            println("❌ DETALLE DE ERROR: ${e.message}")
            throw e
        }
    }
}

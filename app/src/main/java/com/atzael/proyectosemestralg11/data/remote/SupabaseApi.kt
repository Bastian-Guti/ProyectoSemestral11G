package com.atzael.proyectosemestralg11.data.remote

import com.atzael.proyectosemestralg11.model.Cliente
import com.atzael.proyectosemestralg11.model.Equipo
import com.atzael.proyectosemestralg11.model.Local
import com.atzael.proyectosemestralg11.model.OrdenTrabajo
import com.atzael.proyectosemestralg11.model.Usuario
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

interface SupabaseApi {

    // ---------- Usuarios ----------
    @GET("usuarios")
    suspend fun getUsuarios(): List<Usuario>

    @GET("usuarios")
    suspend fun getUsuarioByRut(@Query("rut") rutQuery: String): List<Usuario> // e.g. "eq.111111111"

    @Headers("Prefer: return=representation, resolution=merge-duplicates")
    @POST("usuarios")
    suspend fun crearUsuario(@Body usuario: Usuario): Response<List<Usuario>>

    // ---------- Clientes ----------
    @GET("clientes")
    suspend fun getClientes(): List<Cliente>

    @Headers("Prefer: return=representation, resolution=merge-duplicates")
    @POST("clientes")
    suspend fun crearCliente(@Body cliente: Cliente): Response<List<Cliente>>

    @Headers("Prefer: return=representation")
    @PATCH("clientes")
    suspend fun actualizarCliente(@Query("id") idQuery: String, @Body cliente: Cliente): Response<List<Cliente>>

    // ---------- Locales ----------
    @GET("locales")
    suspend fun getLocales(): List<Local>

    @Headers("Prefer: return=representation, resolution=merge-duplicates")
    @POST("locales")
    suspend fun crearLocal(@Body local: Local): Response<List<Local>>

    @Headers("Prefer: return=representation")
    @PATCH("locales")
    suspend fun actualizarLocal(@Query("id") idQuery: String, @Body local: Local): Response<List<Local>>

    // ---------- Equipos ----------
    @GET("equipos")
    suspend fun getEquipos(): List<Equipo>

    @Headers("Prefer: return=representation, resolution=merge-duplicates")
    @POST("equipos")
    suspend fun crearEquipo(@Body equipo: Equipo): Response<List<Equipo>>

    @Headers("Prefer: return=representation")
    @PATCH("equipos")
    suspend fun actualizarEquipo(@Query("id") idQuery: String, @Body equipo: Equipo): Response<List<Equipo>>

    // ---------- Órdenes de Trabajo ----------
    @GET("ordenes_trabajo")
    suspend fun getOrdenesTrabajo(): List<OrdenTrabajo>

    @Headers("Prefer: return=representation, resolution=merge-duplicates")
    @POST("ordenes_trabajo")
    suspend fun crearOrdenTrabajo(@Body orden: OrdenTrabajo): Response<List<OrdenTrabajo>>

    @Headers("Prefer: return=representation")
    @PATCH("ordenes_trabajo")
    suspend fun actualizarOrdenTrabajo(@Query("id") idQuery: String, @Body orden: OrdenTrabajo): Response<List<OrdenTrabajo>>
}

package com.atzael.proyectosemestralg11.data.remote

import com.atzael.proyectosemestralg11.model.Cliente
import com.atzael.proyectosemestralg11.model.Equipo
import com.atzael.proyectosemestralg11.model.FotoOrden
import com.atzael.proyectosemestralg11.model.Local
import com.atzael.proyectosemestralg11.model.OrdenTrabajo
import com.atzael.proyectosemestralg11.model.Reprogramacion
import com.atzael.proyectosemestralg11.model.Usuario
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

// Los POST de creación usan "resolution=ignore-duplicates": si ya existe una fila con ese id
// (o RUT) en Supabase, se conserva la de Supabase y no se crea otra ni se sobrescribe.
// Los cambios a registros existentes van por PATCH.
interface SupabaseApi {

    // ---------- Usuarios ----------
    @GET("usuarios")
    suspend fun getUsuarios(): List<Usuario>

    @GET("usuarios")
    suspend fun getUsuarioByRut(@Query("rut") rutQuery: String): List<Usuario> // e.g. "eq.111111111"

    @Headers("Prefer: return=representation, resolution=ignore-duplicates")
    @POST("usuarios")
    suspend fun crearUsuario(@Body usuario: Usuario): Response<List<Usuario>>

    // Solo cambia los campos enviados, e.g. mapOf("aviso_datos_aceptado" to true)
    @PATCH("usuarios")
    suspend fun actualizarUsuario(@Query("rut") rutQuery: String, @Body campos: Map<String, @JvmSuppressWildcards Any>): Response<Unit>

    // ---------- Clientes ----------
    @GET("clientes")
    suspend fun getClientes(): List<Cliente>

    @Headers("Prefer: return=representation, resolution=ignore-duplicates")
    @POST("clientes")
    suspend fun crearCliente(@Body cliente: Cliente): Response<List<Cliente>>

    @Headers("Prefer: return=representation")
    @PATCH("clientes")
    suspend fun actualizarCliente(@Query("id") idQuery: String, @Body cliente: Cliente): Response<List<Cliente>>

    // ---------- Locales ----------
    @GET("locales")
    suspend fun getLocales(): List<Local>

    @Headers("Prefer: return=representation, resolution=ignore-duplicates")
    @POST("locales")
    suspend fun crearLocal(@Body local: Local): Response<List<Local>>

    @Headers("Prefer: return=representation")
    @PATCH("locales")
    suspend fun actualizarLocal(@Query("id") idQuery: String, @Body local: Local): Response<List<Local>>

    // ---------- Equipos ----------
    @GET("equipos")
    suspend fun getEquipos(): List<Equipo>

    @Headers("Prefer: return=representation, resolution=ignore-duplicates")
    @POST("equipos")
    suspend fun crearEquipo(@Body equipo: Equipo): Response<List<Equipo>>

    @Headers("Prefer: return=representation")
    @PATCH("equipos")
    suspend fun actualizarEquipo(@Query("id") idQuery: String, @Body equipo: Equipo): Response<List<Equipo>>

    // ---------- Órdenes de Trabajo ----------
    @GET("ordenes_trabajo")
    suspend fun getOrdenesTrabajo(): List<OrdenTrabajo>

    @Headers("Prefer: return=representation, resolution=ignore-duplicates")
    @POST("ordenes_trabajo")
    suspend fun crearOrdenTrabajo(@Body orden: OrdenTrabajo): Response<List<OrdenTrabajo>>

    @Headers("Prefer: return=representation")
    @PATCH("ordenes_trabajo")
    suspend fun actualizarOrdenTrabajo(@Query("id") idQuery: String, @Body orden: OrdenTrabajo): Response<List<OrdenTrabajo>>

    // ---------- Reprogramaciones ----------
    @GET("reprogramaciones")
    suspend fun getReprogramaciones(): List<Reprogramacion>

    @Headers("Prefer: return=representation, resolution=ignore-duplicates")
    @POST("reprogramaciones")
    suspend fun crearReprogramacion(@Body reprogramacion: Reprogramacion): Response<List<Reprogramacion>>

    // ---------- Fotos ----------
    @GET("fotos_orden")
    suspend fun getFotosOrden(): List<FotoOrden>

    // La foto se envía con su id local y la ruta reemplazada por la imagen en Base64
    @Headers("Prefer: return=minimal, resolution=ignore-duplicates")
    @POST("fotos_orden")
    suspend fun crearFotoOrden(@Body foto: FotoOrden): Response<Unit>
}

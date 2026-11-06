package com.atzael.proyectosemestralg11.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.atzael.proyectosemestralg11.model.Cliente
import com.atzael.proyectosemestralg11.model.Equipo
import com.atzael.proyectosemestralg11.model.FotoOrden
import com.atzael.proyectosemestralg11.model.Local
import com.atzael.proyectosemestralg11.model.OrdenTrabajo
import com.atzael.proyectosemestralg11.model.Reprogramacion
import com.atzael.proyectosemestralg11.model.Usuario

@Database(
    entities = [
        Usuario::class,
        Cliente::class,
        Local::class,
        Equipo::class,
        OrdenTrabajo::class,
        Reprogramacion::class,
        FotoOrden::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun clienteDao(): ClienteDao
    abstract fun localDao(): LocalDao
    abstract fun equipoDao(): EquipoDao
    abstract fun ordenTrabajoDao(): OrdenTrabajoDao
    abstract fun fotoDao(): FotoDao

    // Usuarios de prueba.
    //   Jefatura 111111111 / 1    Técnico 222222222 / 2    Cliente 333333333 / 3
    //   Técnicos: Juan Pérez 444444444 / juan, Carlos Gómez 555555555 / carlos, Luis Martínez 666666666 / luis
    private object UsuariosDePrueba : Callback() {
        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)

            // Primero se unifican los RUT con guion de versiones anteriores (evita duplicados)
            normalizarRutsExistentes(db)

            db.execSQL(
                "INSERT OR IGNORE INTO clientes (id, nombre, contacto, telefono, slaHoras) " +
                        "VALUES (1, 'Empresa Demo', 'Cliente Demo', '+56912345678', 24)"
            )
            db.execSQL(
                "INSERT OR IGNORE INTO usuarios (rut, nombre, apellido, password, rol, clienteId) VALUES " +
                        "('111111111', 'Jefe', 'Demo', '1', 'Jefatura', NULL), " +
                        "('222222222', 'Tecnico', 'Demo', '2', 'Tecnico', NULL), " +
                        "('333333333', 'Cliente', 'Demo', '3', 'Cliente', 1), " +
                        "('444444444', 'Juan', 'Pérez', 'juan', 'Tecnico', NULL), " +
                        "('555555555', 'Carlos', 'Gómez', 'carlos', 'Tecnico', NULL), " +
                        "('666666666', 'Luis', 'Martínez', 'luis', 'Tecnico', NULL)"
            )
            // Las contraseñas de prueba siempre vuelven a ser las indicadas arriba. No se borra
            // la fila para no perder el aviso de datos ya aceptado por cada técnico.
            db.execSQL(
                "UPDATE usuarios SET password = CASE rut " +
                        "WHEN '111111111' THEN '1' WHEN '222222222' THEN '2' WHEN '333333333' THEN '3' " +
                        "WHEN '444444444' THEN 'juan' WHEN '555555555' THEN 'carlos' ELSE 'luis' END " +
                        "WHERE rut IN ('111111111', '222222222', '333333333', " +
                        "'444444444', '555555555', '666666666')"
            )
        }


        private fun normalizarRutsExistentes(db: SupportSQLiteDatabase) {
            fun norm(columna: String) = "UPPER(REPLACE(REPLACE($columna, '-', ''), '.', ''))"

            listOf(
                "ordenes_trabajo" to listOf("tecnicoRut", "creadaPor", "conformidadPor"),
                "reprogramaciones" to listOf("tecnicoAnterior", "tecnicoNuevo"),
                "fotos_orden" to listOf("tomadaPor")
            ).forEach { (tabla, columnas) ->
                columnas.forEach { columna ->
                    db.execSQL("UPDATE $tabla SET $columna = ${norm(columna)} WHERE $columna IS NOT NULL AND $columna != ${norm(columna)}")
                }
            }
            db.execSQL(
                "DELETE FROM usuarios WHERE rut != ${norm("rut")} " +
                        "AND ${norm("rut")} IN (SELECT rut FROM usuarios)"
            )
            db.execSQL("UPDATE usuarios SET rut = ${norm("rut")} WHERE rut != ${norm("rut")}")
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "proyecto_database"
                )
                    // Las versiones 2 a 5 cambiaron el esquema. Sin migración: se recrean las tablas.
                    .fallbackToDestructiveMigration(true)
                    .addCallback(UsuariosDePrueba)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

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
    version = 6,
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
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            insertarDatosDemo(db)
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            insertarDatosDemo(db)
        }

        private fun insertarDatosDemo(db: SupportSQLiteDatabase) {
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

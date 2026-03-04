package com.example.das_gaztandegia;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;

public class DataBaseHelper extends SQLiteOpenHelper {

    // Constructor básico de la base de datos
    public DataBaseHelper(@Nullable Context context, @Nullable String name,
                          @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    // Se ejecuta la primera vez que la app necesita la base de datos. Aquí creamos las tablas.
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        // Tabla de trabajadores. Usamos UNIQUE en el email para que no se puedan registrar dos veces con el mismo correo.
        sqLiteDatabase.execSQL("CREATE TABLE Usuarios ('id_usuario' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 'nombre' VARCHAR(255), 'email' VARCHAR(255) UNIQUE, 'password' VARCHAR(255))");

        // Tabla de los quesos. Le metemos la clave foránea (id_usuario_fk) para saber qué trabajador hizo cada lote.
        sqLiteDatabase.execSQL("CREATE TABLE Lotes ('id_lote' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 'fecha' VARCHAR(255), 'temperatura' REAL, 'tiempo_cuajado' INTEGER, 'ph' REAL, 'nota_calidad' VARCHAR(255), 'id_usuario_fk' INTEGER, FOREIGN KEY('id_usuario_fk') REFERENCES Usuarios('id_usuario'))");
    }

    // Si algún día actualizamos la versión de la BD, a lo bruto: borramos las tablas y las hacemos de nuevo.
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS Lotes");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS Usuarios");
        onCreate(sqLiteDatabase);
    }

    /* =========================================================================
       MÉTODOS PARA LOS USUARIOS (LOGIN Y REGISTRO)
       ========================================================================= */

    // Guarda un trabajador nuevo en la BD. Devuelve true si ha ido bien, o false si falla (ej. correo repetido).
    public boolean insertarUsuario(String nombre, String email, String password) {
        SQLiteDatabase bd = getWritableDatabase();

        ContentValues nuevo = new ContentValues();
        nuevo.put("nombre", nombre);
        nuevo.put("email", email);
        nuevo.put("password", password);

        long resultado = bd.insert("Usuarios", null, nuevo);
        bd.close(); // Siempre cerramos para no consumir memoria a lo tonto

        return resultado != -1;
    }

    // Comprueba el login y devuelve el ID del usuario (o -1 si falla)
    public int comprobarLogin(String email, String password) {
        android.database.sqlite.SQLiteDatabase bd = getReadableDatabase();

        String[] argumentos = new String[] {email, password};
        // Hacemos el SELECT
        android.database.Cursor c = bd.rawQuery("SELECT id_usuario FROM Usuarios WHERE email=? AND password=?", argumentos);

        int idTrabajador = -1; // Por defecto es -1 (falso/error)

        // Si el cursor encuentra al usuario, nos movemos a la primera fila y sacamos su ID
        if (c.moveToFirst()) {
            idTrabajador = c.getInt(0); // Columna 0 es id_usuario
        }

        c.close();
        bd.close();

        return idTrabajador;
    }

    /* =========================================================================
       MÉTODOS PARA LOS QUESOS (LOTES)
       ========================================================================= */

    // Guarda un nuevo lote de queso asociándolo a un usuario concreto mediante su ID.
    // Guarda un nuevo lote. Ahora le pasamos nosotros el ID basado en la fecha.
    public boolean insertarLote(int id_lote, String fecha, double temperatura, int tiempo_cuajado, double ph, String nota_calidad, int id_usuario_fk) {
        SQLiteDatabase bd = getWritableDatabase();

        ContentValues nuevo = new ContentValues();
        nuevo.put("id_lote", id_lote); // ¡Añadimos el ID que hemos calculado!
        nuevo.put("fecha", fecha);
        nuevo.put("temperatura", temperatura);
        nuevo.put("tiempo_cuajado", tiempo_cuajado);
        nuevo.put("ph", ph);
        nuevo.put("nota_calidad", nota_calidad);
        nuevo.put("id_usuario_fk", id_usuario_fk);

        // Si intentamos meter un ID que ya existe (dos quesos el mismo día), insert devolverá -1
        long resultado = bd.insert("Lotes", null, nuevo);
        bd.close();

        return resultado != -1;
    }

    // Recupera todo el historial de quesos para poder mostrarlo luego en la pantalla de la lista.
    public Cursor obtenerTodosLosLotes() {
        SQLiteDatabase bd = getReadableDatabase();

        // Ojo: no cerramos el cursor ni la BD aquí, porque la Activity que llame a este método
        // necesita leer los datos primero. Ya los cerrará la Activity cuando termine.
        return bd.rawQuery("SELECT * FROM Lotes", null);
    }

    // Por si nos equivocamos al meter un lote y queremos borrarlo del registro.
    public void borrarLote(int id_lote) {
        SQLiteDatabase bd = getWritableDatabase();

        String[] argumentos = new String[]{String.valueOf(id_lote)};
        bd.delete("Lotes", "id_lote=?", argumentos);

        bd.close();
    }

    // Método para buscar un queso en concreto por su ID
    public android.database.Cursor obtenerLotePorId(int id_lote) {
        android.database.sqlite.SQLiteDatabase bd = getReadableDatabase();
        String[] argumentos = new String[]{String.valueOf(id_lote)};

        // Buscamos solo el que coincida con el ID que hemos tocado
        return bd.rawQuery("SELECT * FROM Lotes WHERE id_lote=?", argumentos);
    }

    // Método para actualizar la nota (las estrellas) de un lote existente
    public void actualizarNotaLote(int id_lote, String nuevaNota) {
        android.database.sqlite.SQLiteDatabase bd = getWritableDatabase();

        android.content.ContentValues valores = new android.content.ContentValues();
        valores.put("nota_calidad", nuevaNota); // Sobrescribimos el campo con las estrellas

        // Hacemos un UPDATE en la tabla Lotes donde el ID coincida
        bd.update("Lotes", valores, "id_lote=?", new String[]{String.valueOf(id_lote)});
        bd.close();
    }
}
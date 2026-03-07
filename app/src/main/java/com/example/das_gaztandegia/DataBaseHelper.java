package com.example.das_gaztandegia;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;

public class DataBaseHelper extends SQLiteOpenHelper {

    public DataBaseHelper(@Nullable Context context, @Nullable String name,
                          @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        // Tabla de trabajadores. Usamos UNIQUE en el email para que no se puedan registrar dos veces con el mismo correo.
        sqLiteDatabase.execSQL("CREATE TABLE Usuarios " +
                "('id_usuario' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "'nombre' VARCHAR(255), " +
                "'email' VARCHAR(255) UNIQUE, " +
                "'password' VARCHAR(255))");

        // Tabla de los quesos. Le metemos la clave foránea (id_usuario_fk) para saber qué trabajador hizo cada lote.
        sqLiteDatabase.execSQL("CREATE TABLE Lotes (" +
                "id_lote INTEGER PRIMARY KEY, " +
                "fecha TEXT, " +
                "temperatura_pasteurizacion REAL, " +
                "temperatura REAL, " +
                "tiempo_cuajado INTEGER, " +
                "ph_corte REAL, " +
                "ph REAL, " +
                "observaciones VARCHAR(255), " +
                "nota_calidad TEXT, " +
                "id_usuario_fk INTEGER)");    }

    // Si algún día actualizamos la versión de la BD, a lo bruto: borramos las tablas y las hacemos de nuevo.
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS Lotes");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS Usuarios");
        onCreate(sqLiteDatabase);
    }



    // Guarda un trabajador nuevo en la BD. Devuelve true si ha ido bien, o false si falla
    public boolean insertarUsuario(String nombre, String email, String password) {
        SQLiteDatabase bd = getWritableDatabase();

        ContentValues nuevo = new ContentValues();
        nuevo.put("nombre", nombre);
        nuevo.put("email", email);
        nuevo.put("password", password);

        long resultado = bd.insert("Usuarios", null, nuevo);
        bd.close();

        return resultado != -1;
    }

    // Comprueba el login y devuelve el ID del usuario y -1 si falla
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

    // Método actualizado con todos los campos
    public boolean insertarLote(int id_lote, String fecha, double temp_past, double temp_cuaj, int tiempo_cuaj, double ph_corte, double ph_final, String observaciones, int id_usuario_fk) {
        android.database.sqlite.SQLiteDatabase bd = getWritableDatabase();

        android.content.ContentValues nuevo = new android.content.ContentValues();
        nuevo.put("id_lote", id_lote);
        nuevo.put("fecha", fecha);
        nuevo.put("temperatura_pasteurizacion", temp_past);
        nuevo.put("temperatura", temp_cuaj);
        nuevo.put("tiempo_cuajado", tiempo_cuaj);
        nuevo.put("ph_corte", ph_corte);
        nuevo.put("ph", ph_final);
        nuevo.put("observaciones", observaciones);
        nuevo.put("nota_calidad", ""); // La nota empieza vacía hasta que el usuario le ponga estrellas
        nuevo.put("id_usuario_fk", id_usuario_fk);

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

    // Método combinado para buscar texto y filtrar por nota mínima
    public android.database.Cursor obtenerLotesFiltrados(String textoBusqueda, float notaMinima) {
        android.database.sqlite.SQLiteDatabase bd = getReadableDatabase();

        // Buscamos si el texto coincide en el ID o en la fecha
        String query = "SELECT * FROM Lotes WHERE (CAST(id_lote AS TEXT) LIKE ? OR fecha LIKE ?)";

        // Si el usuario quiere filtrar por nota (por ejemplo, buscar las de 4+ estrellas)
        if (notaMinima > 0) {
            // Casteamos la nota_calidad a número real para que SQLite sepa compararlo matemáticamente
            query += " AND CAST(nota_calidad AS REAL) >= ?";
            return bd.rawQuery(query, new String[]{"%" + textoBusqueda + "%", "%" + textoBusqueda + "%", String.valueOf(notaMinima)});
        } else {
            // Si notaMinima es 0, mostramos todas las notas
            return bd.rawQuery(query, new String[]{"%" + textoBusqueda + "%", "%" + textoBusqueda + "%"});
        }
    }

    // Método para borrar TODOS los lotes
    public void borrarTodoElHistorial() {
        android.database.sqlite.SQLiteDatabase bd = getWritableDatabase();
        bd.execSQL("DELETE FROM Lotes"); // Borra todo el contenido de la tabla
        bd.close();
    }

    // Método para obtener el nombre de un trabajador a partir de su ID
    public String obtenerNombreUsuario(int idUsuario) {
        android.database.sqlite.SQLiteDatabase bd = this.getReadableDatabase();



        android.database.Cursor cursor = bd.rawQuery("SELECT nombre FROM Usuarios WHERE id_usuario = ?", new String[]{String.valueOf(idUsuario)});

        String nombreEncontrado = "Trabajador"; // Valor por defecto por si falla

        if (cursor.moveToFirst()) {
            nombreEncontrado = cursor.getString(0); // Cogemos la primera columna (el nombre)
        }

        cursor.close();
        bd.close();

        return nombreEncontrado;
    }

    // Método para contar cuántos lotes no tienen nota
    public int contarLotesSinPuntuar() {
        android.database.sqlite.SQLiteDatabase bd = this.getReadableDatabase();
        // Buscamos los que tengan la nota nula o vacía
        android.database.Cursor cursor = bd.rawQuery("SELECT COUNT(*) FROM Lotes WHERE nota_calidad IS NULL OR nota_calidad = ''", null);

        int cantidad = 0;
        if (cursor.moveToFirst()) {
            cantidad = cursor.getInt(0);
        }

        cursor.close();
        bd.close();
        return cantidad;
    }
}